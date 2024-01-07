(ns auth.client
  (:require [ui]
            [auth.core]
            [client.routing]
            [auth.client.routes]
            [vote.client.routes]
            [wire.client]
            [client.toast]
            [client.app]
            [vote.core]
            [core]))


;; 
;; 
;; 
;; 
;; 
;; 
;; 

(core/register-module! ::auth)

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/on-init ::auth []
  {::email ""
   ::current-user-account nil
   ::loading-user-account? true
   ::logging-out? false})

(defn ->auth-state [input]
  (cond
    (-> input ::loading-user-account?) ::loading
    (-> input ::current-user-account nil? not) ::logged-in
    :else ::logged-out))

;; 
;; 
;; 
;; 
;; 
;; Login
;; 
;; 
;; 
;; 
;; 

;; 
;; 
;; Login with email link
;; 
;; 

(defn view-login-with-email-link [{:keys [dispatch!] :as input}]
  [:<>
   [ui/text-field
    {:label "Email"
     :value (::email input) 
     core/msg :email
     :on-value #(dispatch! {core/msg ::user-inputted-email ::inputted-email %})}]
   
   [ui/button
    {:text "Send login link" 
     :on-click #(dispatch! {core/msg ::clicked-send-login-link})}]])

(defmethod core/on-msg ::user-inputted-email [input]
  (assoc input ::email (input ::inputted-email)))

(defmethod core/on-msg ::clicked-send-login-link [input]
  (let [email (-> input ::email)
        to-server (auth.core/->user-clicked-send-login-link-email email)] 
    (-> input
        (wire.client/send-to-server to-server)
        (client.toast/show-toast "Not implemented yet"))))

;; 
;; 
;; Login as guest
;; 
;; 

(defmethod core/on-msg ::user-clicked-continue-as-guest [input]
  (let [to-server {core/msg auth.core/user-clicked-continue-as-guest}] 
    (wire.client/send-to-server input to-server)))

(defn view-login-as-guest [{:keys [dispatch!] }]
  [ui/button
    {:text "Continue as guest"
     :on-click #(dispatch! {core/msg ::user-clicked-continue-as-guest})}])


;; 
;; 
;; Login Route
;; 
;; 

(defmethod core/on-msg auth.core/user-logged-in [input]
  (-> input
      (assoc ::current-user-account (-> input :user-account))
      (assoc ::logging-out? false)
      (client.toast/show-toast (str "Logged in as " (-> input :user-account :user-account/username)))
      (client.routing/push-route (client.routing/default-route))))


(defmethod client.routing/view 
  (auth.client.routes/login) 
  [input] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full.overflow-hidden 
   [:h1.text-5xl.font-bold.w-full.text-left.text-blue-500 "voteonit.app"] 
   [view-login-with-email-link input] 
   [:p.text-neutral-500.lowercase.text-lg "or"]
   [view-login-as-guest input]])



;; 
;; 
;; 
;; 
;; 
;; Logout
;; 
;; 
;; 
;; 
;; 

(defn view-logout-button [{:keys [dispatch!] :as input}]
  [ui/button
     {:text "Logout"
      :loading? (::logging-out? input)
      :on-click #(dispatch! {core/msg ::clicked-logout-button})}])


(defmethod core/on-msg ::clicked-logout-button [input]
  (-> input
      (wire.client/send-to-server {core/msg auth.core/user-clicked-logout-button})
      (assoc ::logging-out? true)))


(defmethod core/on-msg auth.core/user-logged-out [input]
  (-> input
      (assoc ::current-user-account nil)
      (assoc ::logging-out? false)
      (client.toast/show-toast "Logged out")
      (client.routing/push-route (auth.client.routes/login))))


;; 
;; 
;; 
;; Account Screen
;; 
;; 
;; 

(defmethod core/on-msg ::clicked-polls-button [input]
  (-> input
      (client.routing/push-route (vote.client.routes/polls))))

(defn ->current-username [input]
  (-> input ::current-user-account :user-account/username))

(defmethod client.routing/view 
  (auth.client.routes/account)
  [{:keys [dispatch!] :as input}] 
  [:div.w-full.h-full.flex.flex-col
   [ui/top-bar {:title "Account"}] 
   
   [:div.w-full.flex-1.flex.flex-col.gap-4.p-6.items-center
    
    [ui/avatar {:seed (->current-username input) :class "w-32 h-32"}]
    
    [:p.text-2xl.font-bold.text-center.w-full 
     {:class (when (nil? (->current-username input)) "text-transparent")}
     (or (->current-username input) "...")]

    [view-logout-button input]]
   
   [client.app/bottom-bar 
    {:active :account
     :on-polls #(dispatch! {core/msg ::clicked-polls-button})}]])

;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defn handle-auth-redirects [input]
  (let [auth-state (->auth-state input)
        current-route (client.routing/->current-route input)] 
    (cond
      
      (and (= auth-state ::logged-in)
           (= current-route (auth.client.routes/login)))
      (client.routing/push-route input (client.routing/default-route))
      
      (and (= auth-state ::logged-out)
           (not= current-route (auth.client.routes/login)))
      (client.routing/push-route input (auth.client.routes/login))
      
      :else
      input)))

;; 
;; 
;; 
;; Current User
;; 
;; 
;; 

(defmethod core/on-msg auth.core/current-user-account [input]
  (-> input
      (assoc ::current-user-account (-> input :user-account))
      (assoc ::loading-user-account? false)
      (assoc ::logging-out? false)
      handle-auth-redirects))


;; 
;; 
;; 
;; 
;; 
;; 


(defmulti on-evt core/evt)

(defmethod core/on-evt ::auth [input]
  (on-evt input))

(defmethod on-evt :default [input] input)

(defmethod on-evt client.routing/route-changed [input]
  (-> input handle-auth-redirects))

;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 


(defmethod core/msgs! ::auth [])
