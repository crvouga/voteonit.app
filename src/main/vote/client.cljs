(ns vote.client
  (:require [client.routing]
            [ui.button]
            [auth.client]
            [core]))


(defn initial-state []
  {::polls-by-id {}})

(defmethod core/handle-msg ::clicked-open-account-button [input]
  (-> input auth.client/open-account-screen))

(defmethod client.routing/view-route :polls [{:keys [dispatch!]}]
  [:div "polls"
  [ui.button/view 
   {:text "Open account" :on-click #(dispatch! {:type ::clicked-open-account-button})}]])