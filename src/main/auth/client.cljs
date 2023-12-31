(ns auth.client
  (:require [ui.textfield]
            [ui.button]
            [auth.core]
            [wire.client]
            [core :refer [handle-msg]]))

;; 
;; 
;; 
;; 
;; 
;; 

(defn initial-state []
  {::email ""
   ::current-user-account nil})

(defmethod handle-msg ::user-inputted-email [input]
  (assoc-in input  [:state ::email] (-> input :msg :email)))

(defmethod handle-msg ::clicked-send-login-link [input]
  (let [email (-> input :state ::email)
        to-server {:type auth.core/user-clicked-send-login-link-email :email email}
        output (wire.client/send-to-server input to-server)] 
    output))

(defmethod handle-msg ::clicked-continue-as-guest [input]
  (let [to-server {:type auth.core/user-clicked-continue-as-guest}
        output (wire.client/send-to-server input to-server)] 
    output))

(defmethod handle-msg auth.core/current-user-account [input]
  (assoc-in input [:state ::current-user-account] (input :msg)))

(defn to-auth-state [{:keys [state]}]
  (if (-> state ::current-user-account :user-id string?)
    :logged-in
    :logged-out))

;; 
;; 
;; 
;; 
;; 

(defn view-login-page [{:keys [state dispatch!]}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6.h-full
   [:h1.text-5xl.font-bold.w-full.text-left.text-blue-500 "voteonit.app"]
   
   [ui.textfield/view 
    {:label "Email"
     :value (::email state) 
     :type :email
     :on-value #(dispatch! {:type ::user-inputted-email :email %})}]
   
   [ui.button/view 
    {:text "Send login link" 
     :on-click #(dispatch! {:type ::clicked-send-login-link})}]
   
   [:p.text-neutral-500.lowercase.text-lg "or"]

   [ui.button/view 
    {:text "Continue as guest"
     :on-click #(dispatch! {:type ::clicked-continue-as-guest})}]])