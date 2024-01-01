(ns client.toast
  (:require [core :refer [handle-command append-command handle-msg]]
            [cljs.core.async :refer [go-loop timeout <!]]))

;; 
;; 
;; 
;; State
;; 
;; 
;; 

(defn initial-state []
  {::toast nil})

;; 
;; 
;; Toast
;; 
;; 

(defn new-toast [message]
  {:message message
   :duration 3000
   :created-at (js/Date.now())})

(defn toast-expired? [toast]
  (let [duration (-> toast :duration)
        created-at (-> toast :created-at)
        now (js/Date.now())
        elapsed (- now created-at)]
    (>= elapsed duration)))

;; 
;; 
;; Command
;; 
;; 

(defmethod handle-command ::show-toast [input] 
  (let [message (-> input :command :message)
        toast (new-toast message)]
    (-> input 
        (assoc-in [:state ::toast] toast))))

(defn show-toast [input message]
  (append-command input {:type ::show-toast :message message}))

;; 
;; 
;; Msg
;; 
;; 

(defmethod handle-msg ::time-passed [input]
  (let [toast (-> input :state ::toast)
        removed (assoc-in input [:state ::toast] nil)
        output (if (toast-expired? toast) removed input)]
    output))

;; 
;; 
;; View
;; 
;; 



(defn view [{:keys [state]}]
  (let [message (-> state ::toast :message)]
     (when message
       [:div.absolute.inset-0.flex.items-start.justify-center.pointer-events-none.p-4
        [:div.w-full.px-4.p-2.text-white.bg-neutral-700.rounded.text-base.font-semibold.transition-all
         
         message]])))

;; 
;; 
;; Subscriptions
;; 
;; 

(defn subscriptions! [state! dispatch!]
  (go-loop []
    (when (not (nil? (-> @state! ::toast)))
      (dispatch! {:type ::time-passed}))
    (<! (timeout 1000))
    (recur)))