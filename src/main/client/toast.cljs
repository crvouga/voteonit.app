(ns client.toast
  (:require [core :refer [handle-msg append-effect handle-command]]
            [client.core]))


(defn initial-state []
  {::toast nil})

(defmethod handle-msg ::toast-time-ellaspsed [input]
  input)

(defmethod handle-command :show-toast [input]
  (let [message (-> input :command :message)]
    (-> input (update ::toast assoc :message message)
              (append-effect :toast-time-ellaspsed))))

(defn view [{:keys [state]}]
  (let [toast (-> state ::toast)]
    (when toast
      [:div.absolute.inset-0
       [:div.message (-> toast :message)]])))