(ns client.toast
  (:require [core :refer [handle-msg add-eff handle-cmd]]
            [client.core]))


(defn initial-state []
  {::toast nil})

(defmethod handle-msg ::toast-time-ellaspsed [input]
  input)

(defmethod handle-cmd :show-toast [input]
  (let [message (-> input :cmd :message)]
    (-> input (update ::toast assoc :message message)
              (add-eff :toast-time-ellaspsed 5000))))

(defn view [{:keys [state]}]
  (let [toast (-> state ::toast)]
    (when toast
      [:div.absolute.inset-0
       [:div.message (-> toast :message)]])))