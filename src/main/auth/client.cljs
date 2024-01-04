(ns auth.client
  (:require [ui.textfield]
            [ui.button]
            [auth.core]
            [client.routing]
            [wire.client]
            [client.toast]
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
;; 
;; 
;; 

(defmethod core/on-evt [::auth :something-happend] [input]
  (println "Something happend" input)
  input)

;; 
;; 
;; 
;; Toast
;; 
;; 
;; 


(defn to-auth-state [input]
  (cond
    (-> input ::loading-user-account?) :loading
    (-> input ::current-user-account nil? not) :logged-in
    :else :logged-out))


(defn to-toast-message [input]
  (let [auth-state (to-auth-state input)]
    (cond
      (= auth-state :logged-in) "Logged in"
      (= auth-state :logged-out) "Logged out"
      :else nil)))

(defn show-auth-state-toast [input]
  (let [message (to-toast-message input)]
    (if message
      (client.toast/show-toast input message)
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
      (assoc ::current-user-account (-> input core/msg :account))
      (assoc ::loading-user-account? false)
      (assoc ::logging-out? false)))

(defmethod core/on-msg ::user-clicked-logout-button [input]
  (-> input
      (wire.client/send-to-server {:type auth.core/user-clicked-logout-button})
      (assoc ::logging-out? true)))

(defmethod core/on-msg auth.core/user-logged-out [input]
  (-> input
      (assoc ::current-user-account nil)
      (assoc ::logging-out? false)
      (client.routing/push-route :login-route)
      show-auth-state-toast))

(defmethod core/on-msg auth.core/user-logged-in [input]
  (-> input
      (assoc ::current-user-account (-> input core/msg :account))
      (assoc ::logging-out? false)
      show-auth-state-toast))


;; 
;; 
;; 
;; 
;; 
;; 

(defn push-route-account [input]
  (-> input (client.routing/push-route {:type ::account})))


(defmethod client.routing/location->route "/account" [_]
  {:type ::account})

(defmethod client.routing/route->location ::account []
  {:pathname "/account"})

(defmethod client.routing/location->route "/login" [_]
  {:type ::login})

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
  (let [to-server (core/msg auth.core/user-clicked-continue-as-guest)
        output (wire.client/send-to-server input to-server)] 
    output))


(defmethod client.routing/view-route ::login [{:keys [dispatch!] :as input}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   [:h1.text-5xl.font-bold.w-full.text-left.text-blue-500 "voteonit.app"]
   
   [ui.textfield/view 
    {:label "Email"
     :value (::email input) 
     :type :email
     :on-value #(dispatch! {core/msg ::user-inputted-email ::inputted-email %})}]
   
   [ui.button/view 
    {:text "Send login link" 
     :on-click #(dispatch! {core/msg auth.core/user-clicked-send-login-link-email})}]
   
   [:p.text-neutral-500.lowercase.text-lg "or"]

   [ui.button/view 
    {:text "Continue as guest"
     :on-click #(dispatch! {:type ::user-clicked-continue-as-guest})}]])



;; 
;; 
;; 
;; 
;; Account Screen
;; 
;; 
;; 
;; 

(defmethod core/on-msg ::clicked-back-button [input]
  (-> input client.routing/pop-route))



(defmethod client.routing/location->route ::auth [input]
  {"/login" {:type ::login}
   "/account" {:type ::account}})

client.routing/location->route

(defmethod client.routing/view-route ::account [{:keys [dispatch!] :as input}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   [:p.text-xl.font-bold "Account"]
   [ui.button/view {:text "Back" :on-click #(dispatch! {:type ::clicked-back-button})}]
   [ui.button/view 
    {:text "Logout"
     :loading? (::logging-out? input)
     :on-click #(dispatch! {:type ::user-clicked-logout-button})}]])