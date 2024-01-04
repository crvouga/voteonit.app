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

(defmethod core/initial-state ::auth []
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

(defmethod core/handle-event [::auth :something-happend] [input]
  (println "Something happend" input)
  input)

;; 
;; 
;; 
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
;; 
;; 
;; 

(defmethod core/handle-msg ::user-inputted-email [input]
  (assoc input ::email (-> input :msg :email)))

(defmethod core/handle-msg ::clicked-send-login-link [input]
  (let [email (-> input ::email)
        to-server {:type auth.core/user-clicked-send-login-link-email :email email}
        output (wire.client/send-to-server input to-server)] 
    output))

(defmethod core/handle-msg ::user-clicked-continue-as-guest [input]
  (let [to-server {:type auth.core/user-clicked-continue-as-guest}
        output (wire.client/send-to-server input to-server)] 
    output))


(defmethod core/handle-msg auth.core/current-user-account [input]
  (-> input
      (assoc ::current-user-account (-> input :msg :account))
      (assoc ::loading-user-account? false)
      (assoc ::logging-out? false)))

(defmethod core/handle-msg ::user-clicked-logout-button [input]
  (-> input
      (wire.client/send-to-server {:type auth.core/user-clicked-logout-button})
      (assoc ::logging-out? true)))

(defmethod core/handle-msg auth.core/user-logged-out [input]
  (-> input
      (assoc ::current-user-account nil)
      (assoc ::logging-out? false)
      (client.routing/push-route :login-route)
      show-auth-state-toast))

(defmethod core/handle-msg auth.core/user-logged-in [input]
  (-> input
      (assoc ::current-user-account (-> input :msg :account))
      (assoc ::logging-out? false)
      show-auth-state-toast))

(defmethod core/handle-msg ::clicked-back-button [input]
  (-> input client.routing/pop-route))


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
;; 

(defmethod client.routing/view-route ::account [{:keys [dispatch!] :as input}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   [:p.text-xl.font-bold "Account"]
   [ui.button/view {:text "Back" :on-click #(dispatch! {:type ::clicked-back-button})}]
   [ui.button/view 
    {:text "Logout"
     :loading? (::logging-out? input)
     :on-click #(dispatch! {:type ::user-clicked-logout-button})}]])

(defmethod client.routing/view-route ::login [{:keys [dispatch!] :as input}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   [:h1.text-5xl.font-bold.w-full.text-left.text-blue-500 "voteonit.app"]
   
   [ui.textfield/view 
    {:label "Email"
     :value (::email input) 
     :type :email
     :on-value #(dispatch! {:type ::user-inputted-email :email %})}]
   
   [ui.button/view 
    {:text "Send login link" 
     :on-click #(dispatch! {:type ::user-clicked-send-login-link})}]
   
   [:p.text-neutral-500.lowercase.text-lg "or"]

   [ui.button/view 
    {:text "Continue as guest"
     :on-click #(dispatch! {:type ::user-clicked-continue-as-guest})}]])

