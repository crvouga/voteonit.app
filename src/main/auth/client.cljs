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
;; Toast
;; 
;; 
;; 


(defn to-auth-state [input]
  (cond
    (-> input ::loading-user-account?) ::loading
    (-> input ::current-user-account nil? not) ::logged-in
    :else ::logged-out))

(defn logged-out? [input]
  (= (to-auth-state input) ::logged-out))


(defn to-toast-message [input]
  (let [auth-state (to-auth-state input)]
    (cond
      (= auth-state ::logged-in) "Logged in"
      (= auth-state ::logged-out) "Logged out"
      :else nil)))

(defn show-auth-state-toast [input]
  (let [message (to-toast-message input)]
    (if message
      (client.toast/show-toast input message)
      input)))



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

(defn route-login [] 
  {client.routing/path ::route-login})

(defmethod client.routing/view ::route-login [{:keys [dispatch!] :as input}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   [:h1.text-5xl.font-bold.w-full.text-left.text-blue-500 "voteonit.app"]
   
   [ui.textfield/view 
    {:label "Email"
     :value (::email input) 
     core/msg :email
     :on-value #(dispatch! {core/msg ::user-inputted-email ::inputted-email %})}]
   
   [ui.button/view 
    {:text "Send login link" 
     :on-click #(dispatch! {core/msg auth.core/user-clicked-send-login-link-email})}]
   
   [:p.text-neutral-500.lowercase.text-lg "or"]

   [ui.button/view 
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

(defn route-account [] 
  {client.routing/path ::route-account})

(defmethod client.routing/view ::route-account [{:keys [dispatch!] :as input}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   
   [:p.text-xl.font-bold "Account"]
   
   [ui.button/view 
    {:text "Back" 
     :on-click #(dispatch! {core/msg ::clicked-account-screen-back-button})}]
   
   [ui.button/view 
    {:text "Logout"
     :loading? (::logging-out? input)
     :on-click #(dispatch! {core/msg ::clicked-logout-button})}]])



;; 
;; 
;; 
;; Current User
;; 
;; 
;; 

(defmethod core/on-msg auth.core/current-user-account [input]
  (-> input
      (assoc ::current-user-account (-> input :account))
      (assoc ::loading-user-account? false)
      (assoc ::logging-out? false)))

(defmethod core/on-msg auth.core/user-logged-out [input]
  (-> input
      (assoc ::current-user-account nil)
      (assoc ::logging-out? false)
      (client.routing/push-route (route-login))
      show-auth-state-toast))

(defmethod core/on-msg auth.core/user-logged-in [input]
  (-> input
      (assoc ::current-user-account (-> input :account))
      (assoc ::logging-out? false)
      show-auth-state-toast))


(defn redirect-to-login-if-logged-out [input]
  (let [path (client.routing/->current-route-path input)
        redirect? (and (logged-out? input) (not= path ::route-login))
        redirected (client.routing/push-route input (route-login))
        output (if redirect? redirected input)]
    output))

(defmulti on-evt core/evt)

(defmethod core/on-evt ::auth [input]
  (on-evt input))

(defmethod on-evt :default [input] input)

(defmethod on-evt client.routing/route-changed [input]
  (println "on-evt" input)
  (-> input redirect-to-login-if-logged-out))

;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 


(defmethod core/msgs! ::auth [])
