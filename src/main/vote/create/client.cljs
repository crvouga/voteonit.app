(ns vote.create.client
  (:require [client.routing]
            [client.app]
            [vote.core]
            [ui.icon]
            [client.toast]
            [auth.client.routes]
            [vote.client.routes]
            [ui]
            [core]))

(core/register-module! ::vote-create)

(defmethod core/on-init ::vote-create []
  {::question nil})


(defn view-fab [{:keys [dispatch!]}]
  [:div.absolute.inset-0.flex.flex-col.items-end.justify-end.p-6.pointer-events-none
   [:button.rounded-full.overflow-hidden.p-4.bg-blue-500.active:opacity-50.pointer-events-auto
    [ui.icon/plus {:class "w-8 h-8 text-white"
                   :on-click #(dispatch! {core/msg ::clicked-create-poll-fab})}]]])

(defmethod core/on-msg ::clicked-create-poll-fab [input]
  (-> input (client.routing/push-route (vote.client.routes/create-poll))))

(defmethod core/on-msg ::clicked-create-button [input]
  (-> input (client.toast/show-toast "Not implemented yet!")))

(defmethod core/on-msg ::clicked-back-button [input]
  (-> input (client.routing/push-route (vote.client.routes/polls))))

(defmethod client.routing/view
  (vote.client.routes/create-poll)
  [{:keys [dispatch!]}]
  [:div.w-full.h-full.flex.flex-col
   [ui/top-bar {:title "Create Poll"}]
   [:div.flex-1]
   [:div.w-full.flex.items-center.gap-6.p-6.border-t.border-neutral-700
    [ui/button 
     {:text "Back"
      :on-click #(dispatch! {core/msg ::clicked-back-button})}]
    [ui/button 
     {:text "Create" 
      :on-click #(dispatch! {core/msg ::clicked-create-button})}]]])

