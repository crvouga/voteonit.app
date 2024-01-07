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

;; 
;; 
;; 
;; Toast
;; 
;; 
;; 


(defn ->auth-state [input]
  (cond
    (-> input ::loading-user-account?) ::loading
    (-> input ::current-user-account nil? not) ::logged-in
    :else ::logged-out))

;; 
;; 
;; 
;; 
;; Login Screen
;; 
;; 
;; 
;; 

(defmethod core/on-msg ::user-inputted-email [input]
  (assoc input ::email (input ::inputted-email)))

(defmethod core/on-msg ::clicked-send-login-link [input]
  (let [email (-> input ::email)
        to-server (auth.core/->user-clicked-send-login-link-email email)
        output (wire.client/send-to-server input to-server)] 
    output))

(defmethod core/on-msg ::user-clicked-continue-as-guest [input]
  (let [to-server {core/msg auth.core/user-clicked-continue-as-guest}] 
    (wire.client/send-to-server input to-server)))


(defmethod client.routing/view-path 
  auth.client.routes/path-login 
  [{:keys [dispatch!] :as input}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full.overflow-hidden 

   [:h1.text-5xl.font-bold.w-full.text-left.text-blue-500 "voteonit.app"]
   
   [ui/text-field
    {:label "Email"
     :value (::email input) 
     core/msg :email
     :on-value #(dispatch! {core/msg ::user-inputted-email ::inputted-email %})}]
   
   [ui/button
    {:text "Send login link" 
     :on-click #(dispatch! {core/msg auth.core/user-clicked-send-login-link-email})}]
   
   [:p.text-neutral-500.lowercase.text-lg "or"]

   [ui/button
    {:text "Continue as guest"
     :on-click #(dispatch! {core/msg ::user-clicked-continue-as-guest})}]])



;; 
;; 
;; 
;; 
;; Account Screen
;; 
;; 
;; 
;; 

(defmethod core/on-msg ::clicked-account-screen-back-button [input]
  (-> input client.routing/pop-route))

(defmethod core/on-msg ::clicked-logout-button [input]
  (-> input
      (wire.client/send-to-server {core/msg auth.core/user-clicked-logout-button})
      (assoc ::logging-out? true)))

(defmethod core/on-msg ::clicked-polls-button [input]
  (-> input (client.routing/push-route (vote.client.routes/route-polls))))

(defmethod client.routing/view-path auth.client.routes/path-account [{:keys [dispatch!] :as input}] 
  [:div.w-full.h-full.flex.flex-col
   [ui/top-bar {:title "Account"}] 
   
   [:div.w-full.flex-1.flex.flex-col.gap-4.px-6
    [ui/button 
     {:text "Back" 
      :on-click #(dispatch! {core/msg ::clicked-account-screen-back-button})}] 
    
    [ui/button
     {:text "Logout"
      :loading? (::logging-out? input)
      :on-click #(dispatch! {core/msg ::clicked-logout-button})}]]
   
   [client.app/bottom-bar 
    {:active :account
     :on-polls #(dispatch! {core/msg ::clicked-polls-button})
     :on-account #()}]])

;; 
;; 
;; 
;; Current User
;; 
;; 
;; 


(defn handle-auth-redirects [input]
  (let [auth-state (->auth-state input)
        path (client.routing/->current-route-path input)] 
    (cond
      
      (and (= auth-state ::logged-in)
           (= path auth.client.routes/path-login))
      (client.routing/push-route input client.routing/default-route)
      
      (and (= auth-state ::logged-out)
           (not (= path auth.client.routes/path-login)))
      (client.routing/push-route input (auth.client.routes/route-login)) 
      
      :else
      input)))

(defmethod core/on-msg auth.core/current-user-account [input]
  (-> input
      (assoc ::current-user-account (-> input :account))
      (assoc ::loading-user-account? false)
      (assoc ::logging-out? false)
      handle-auth-redirects))

(defmethod core/on-msg auth.core/user-logged-out [input]
  (-> input
      (assoc ::current-user-account nil)
      (assoc ::logging-out? false)
      (client.toast/show-toast "Logged out")
      handle-auth-redirects))


(defmethod core/on-msg auth.core/user-logged-in [input]
  (-> input
      (assoc ::current-user-account (-> input :account))
      (assoc ::logging-out? false)
      (client.toast/show-toast (str "Logged in as " (-> input :account :username)))
      handle-auth-redirects))

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
