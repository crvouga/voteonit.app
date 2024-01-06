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

(defmethod core/on-msg ::clicked-open-account-button [input]
  (-> input (client.routing/push-route auth.client/route-account)))

(def route-polls {:type ::polls})

(defmethod client.routing/view-route ::polls [{:keys [dispatch!]}]
  [:div "polls"
  [ui.button/view 
   {:text "Open account" :on-click #(dispatch! {:type ::clicked-open-account-button})}]])

(defmethod core/msgs! ::vote [])