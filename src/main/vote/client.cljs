(ns vote.client
  (:require [client.routing]
            [ui.button]
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

(defn route-polls []
  {client.routing/path ::route-polls})

(defn view-polls-screen [{:keys [dispatch!]}]
  [:div "polls"
  [ui.button/view 
   {:text "Open account" 
    :on-click #(dispatch! {core/msg ::clicked-open-account-button})}]])

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