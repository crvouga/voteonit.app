(ns vote.create.client
  (:require [client.routing]
            [client.app]
            [vote.core]
            [ui.icon]
            [vote.create.core]
            [wire.client]
            [client.toast]
            [auth.client.routes]
            [vote.client.routes]
            [ui]
            [core]))

;; 
;; 
;; 
;; 
;; 

(core/register-module! ::vote-create)

(defmethod core/on-init ::vote-create []
  {::question ""})


;; 
;; 
;; Fab
;; 
;; 

(defn view-fab [{:keys [dispatch!]}]
  [:div.absolute.inset-0.flex.flex-col.items-end.justify-end.p-6.pointer-events-none
   [:button.rounded-full.overflow-hidden.p-4.bg-blue-500.active:opacity-50.pointer-events-auto
    [ui.icon/plus {:class "w-8 h-8 text-white"
                   :on-click #(dispatch! {core/msg ::clicked-create-poll-fab})}]]])

(defmethod core/on-msg ::clicked-create-poll-fab [input]
  (-> input (client.routing/push-route (vote.client.routes/create-poll))))

;; 
;; 
;; Question Input
;; 
;; 

(defn view-question-input [{:keys [dispatch!] :as input}]
  [ui/text-field
   {:label "Question"
    :value (-> input ::question)
    :on-value #(dispatch! {core/msg ::inputted-question :question %})}])

(defmethod core/on-msg ::inputted-question [input]
  (assoc input ::question (-> input :question)))


;; 
;; 
;; Submit
;; 
;; 

(defn view-create-button [{:keys [dispatch!]}]
  [ui/button
   {:text "Create"
    :on-click #(dispatch! {core/msg ::clicked-create-button})}])

(defmethod core/on-msg ::clicked-create-button [input]
  (-> input
      (wire.client/send-to-server {core/msg vote.create.core/create-poll})
      (client.toast/show-toast "Not implemented yet!")))

(defmethod core/on-msg vote.create.core/create-poll-errored [input]
  (-> input
      (client.toast/show-toast "Error creating poll")))

(defmethod core/on-msg vote.create.core/create-poll-ok [input]
  (-> input
      (client.toast/show-toast "Poll created!")
      (client.routing/push-route (vote.client.routes/poll-details (-> input :poll-id)))))

;; 
;; 
;; Back
;; 
;; 

  (defn view-back-button [{:keys [dispatch!]}]
    [ui/button
     {:text "Back"
      :on-click #(dispatch! {core/msg ::clicked-back-button})}])

  (defmethod core/on-msg ::clicked-back-button [input]
    (-> input (client.routing/push-route (vote.client.routes/polls))))

;; 
;; 
;; View
;; 
;; 

  (defmethod client.routing/view
    (vote.client.routes/create-poll)
    [input]
    [:div.w-full.h-full.flex.flex-col
     [ui/top-bar {:title "Create Poll"}]

     [:div.flex-1.flex.flex-col.p-6.gap-6
      [view-question-input input]]

     [:div.w-full.flex.items-center.gap-6.p-6.border-t.border-neutral-700
      [view-back-button input]
      [view-create-button input]]])

