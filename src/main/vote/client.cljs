(ns vote.client
  (:require [client.routing]
            [ui.button]
            [ui.topbar]
            [auth.client]
            [core]))

(core/register-module! ::vote)

(defmethod core/on-init ::vote []
  {::polls-by-id {}
   ::name nil
   ::questions []})


;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/on-msg ::clicked-open-account-button [input]
  (-> input 
      (client.routing/push-route (auth.client/route-account))))


(defn view-polls-screen [{:keys [dispatch!]}]
  [:div.w-full.h-full.flex.flex-col
   [ui.topbar/view {:title "Polls"}]
   [:div.flex-1.w-full.p-6
    [ui.button/view 
     {:text "Open account" 
     :on-click #(dispatch! {core/msg ::clicked-open-account-button})}]]])

(defmethod client.routing/view-path ::route-polls [input]
  [view-polls-screen input])

(defmethod client.routing/view-path nil [input]
  [view-polls-screen input])

;; 
;; 
;; 
;; 
;; 
;; 


(defmethod core/msgs! ::vote [])