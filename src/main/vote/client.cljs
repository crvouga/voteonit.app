(ns vote.client
  (:require [client.routing]
            [client.app]
            [vote.core]
            [ui.icon]
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
      (client.routing/push-route (auth.client.routes/account))))


(defn view-polls-screen [{:keys [dispatch!]}]
  [:div.flex.flex-col.h-full.w-full
   
   [ui/top-bar {:title "Polls"}]
   
   [:div.flex-1.w-full.p-6.flex.flex-col.relative
    [:div.absolute.inset-0.flex.flex-col.items-end.justify-end.p-6.pointer-events-none
     [:button.rounded-full.overflow-hidden.p-4.bg-blue-500.active:opacity-50.pointer-events-auto
      [ui.icon/plus {:class "w-8 h-8 text-white"}]]]]
   
   [client.app/bottom-bar
     {:active :polls
      :on-account #(dispatch! {core/msg ::clicked-account-button})}]])

(defmethod client.routing/view 
  (vote.client.routes/polls)
  [input]
  [view-polls-screen input])

(defmethod client.routing/view 
  (client.routing/default-route)
  [input]
  [view-polls-screen input])

;; 
;; 
;; 
;; 
;; 
;; 


(defmethod core/msgs! ::vote [])