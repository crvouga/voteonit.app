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
;; 
;; 

(defn initial-state []
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


(defn to-auth-state [{:keys [state]}]
  (cond
    (-> state ::loading-user-account?) :loading
    (-> state ::current-user-account nil? not) :logged-in
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
  (assoc-in input  [:state ::email] (-> input :msg :email)))

(defmethod core/handle-msg ::clicked-send-login-link [input]
  (let [email (-> input :state ::email)
        to-server {:type auth.core/user-clicked-send-login-link-email :email email}
        output (wire.client/send-to-server input to-server)] 
    output))

(defmethod core/handle-msg ::user-clicked-continue-as-guest [input]
  (let [to-server {:type auth.core/user-clicked-continue-as-guest}
        output (wire.client/send-to-server input to-server)] 
    output))


(defmethod core/handle-msg auth.core/current-user-account [input]
  (-> input
      (assoc-in [:state ::current-user-account] (-> input :msg :account))
      (assoc-in [:state ::loading-user-account?] false)
      (assoc-in [:state ::logging-out?] false)))

(defmethod core/handle-msg ::user-clicked-logout-button [input]
  (-> input
      (wire.client/send-to-server {:type auth.core/user-clicked-logout-button})
      (assoc-in [:state ::logging-out?] true)))

(defmethod core/handle-msg auth.core/user-logged-out [input]
  (-> input
      (assoc-in [:state ::current-user-account] nil)
      (assoc-in [:state ::logging-out?] false)
      show-auth-state-toast))

(defmethod core/handle-msg auth.core/user-logged-in [input]
  (-> input
      (assoc-in [:state ::current-user-account] (-> input :msg :account))
      (assoc-in [:state ::logging-out?] false)
      show-auth-state-toast))

(defmethod core/handle-msg ::clicked-back-button [input]
  (-> input client.routing/pop-route))


;; 
;; 
;; 
;; 
;; 
;; 

(defn open-account-screen [input]
  (-> input (client.routing/push-route {:type :acount-route})))
;; 
;; 
;; 
;; 
;; 

(defmethod client.routing/view-route :acount-route [{:keys [state dispatch!]}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   [:p.text-xl.font-bold "Account"]
   [ui.button/view {:text "Back" :on-click #(dispatch! {:type ::clicked-back-button})}]
   [ui.button/view 
    {:text "Logout"
     :loading? (::logging-out? state)
     :on-click #(dispatch! {:type ::user-clicked-logout-button})}]])

(defn view-login-screen [{:keys [state dispatch!]}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   [:h1.text-5xl.font-bold.w-full.text-left.text-blue-500 "voteonit.app"]
   
   [ui.textfield/view 
    {:label "Email"
     :value (::email state) 
     :type :email
     :on-value #(dispatch! {:type ::user-inputted-email :email %})}]
   
   [ui.button/view 
    {:text "Send login link" 
     :on-click #(dispatch! {:type ::user-clicked-send-login-link})}]
   
   [:p.text-neutral-500.lowercase.text-lg "or"]

   [ui.button/view 
    {:text "Continue as guest"
     :on-click #(dispatch! {:type ::user-clicked-continue-as-guest})}]])

