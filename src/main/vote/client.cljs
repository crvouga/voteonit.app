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

(defmethod client.routing/view ::route-polls [{:keys [dispatch!]}]
  [:div "polls"
  [ui.button/view 
   {:text "Open account" 
    :on-click #(dispatch! {core/msg ::clicked-open-account-button})}]])

;; 
;; 
;; 
;; 

(defmethod core/on-msg ::clicked-go-home-button [input]
  (-> input (client.routing/push-route (route-polls))))

(defmethod client.routing/view :default [{:keys [dispatch!]}]
  [:div 
   "Page not found"
   [ui.button/view 
    {:text "Go Home" 
     :on-click #(dispatch! {core/msg ::clicked-go-home-button})}]])


;; 
;; 
;; 
;; 
;; 
;; 


(defmethod core/msgs! ::vote [])