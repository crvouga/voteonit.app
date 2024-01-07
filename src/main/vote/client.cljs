(ns vote.client
  (:require [client.routing]
            [client.app]
            [vote.core]
            [auth.client.routes]
            [vote.client.routes]
            [ui]
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

(defmethod core/on-msg ::clicked-account-button [input]
  (-> input 
      (client.routing/push-route (auth.client.routes/route-account))))


(defn view-polls-screen [{:keys [dispatch!]}]
  [:div.flex.flex-col.h-full.w-full
   
   [ui/top-bar {:title "Polls"}]
   
   [:div.flex-1.w-full.p-6.flex.flex-col]
   
   [client.app/bottom-bar
     {:active :polls
      :on-account #(dispatch! {core/msg ::clicked-account-button})}]])

(defmethod client.routing/view-path 
  vote.client.routes/path-polls 
  [input]
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