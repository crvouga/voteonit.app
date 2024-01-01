(ns client.toast
  (:require [core :refer [handle-msg handle-command append-command]]))


(defn initial-state []
  {::toast nil})

(defmethod handle-msg ::toast-time-ellaspsed [input]
  input)

(defn new-toast [message]
  {:message message})

(defmethod handle-command ::show-toast [input] 
  (let [message (-> input :command :message)
        toast (new-toast message)]
    (-> input 
        (assoc-in [:state ::toast] toast))))

(defn show-toast [input message]
  (append-command input {:type ::show-toast :message message}))

(defn view [{:keys [state]}]
  (let [message (-> state ::toast :message)]
     (when message
       [:div.absolute.inset-0.flex.items-start.justify-center.pointer-events-none
        [:div.w-full.p-4.text-white.bg-neutral-800.rounded.text-lg.font-bold
         message]])))