(ns vote.client
  (:require [client.routing]
            [ui.button]
            [auth.client]
            [core]))

(core/register-module! ::vote)

(defmethod core/initial-state ::vote []
  {::polls-by-id {}
   ::name nil
   ::questions []})

(defmethod core/handle-msg ::clicked-open-account-button [input]
  (-> input auth.client/push-route-account))

(defmethod client.routing/location->route "/" [_input]
  {:type ::polls})

(defmethod client.routing/view-route ::polls [{:keys [dispatch!]}]
  [:div "polls"
  [ui.button/view 
   {:text "Open account" :on-click #(dispatch! {:type ::clicked-open-account-button})}]])