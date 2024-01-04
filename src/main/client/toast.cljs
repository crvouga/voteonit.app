(ns client.toast
  (:require [core]
            [cljs.core.async :refer [go-loop timeout <!]]))

;; 
;; 
;; 
;; 
;; 
;; 

(core/register-module! ::toast)

;; 
;; 
;; 
;; State
;; 
;; 
;; 

(defmethod core/on-init ::toast []
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

(defmethod core/on-cmd ::show-toast [input] 
  (let [message (-> input :cmd :message)
        toast (new-toast message)]
    (-> input 
        (assoc ::toast toast))))

(defn show-toast [input message]
  (core/append-command input {:type ::show-toast :message message}))

;; 
;; 
;; Msg
;; 
;; 

(defmethod core/on-msg ::time-passed [input]
  (let [toast (-> input ::toast)
        removed (assoc input ::toast nil)
        output (if (toast-expired? toast) removed input)]
    output))

;; 
;; 
;; View
;; 
;; 





(defn view [input]
  (let [message (-> input ::toast :message)]
    [:div.absolute.inset-0.flex.items-start.justify-center.pointer-events-none.p-4
     [:div.w-full.px-4.p-2.text-white.bg-neutral-700.rounded.text-base.transition-all
      {:class (if (nil? message) "opacity-0" "opacity-100")}
      message]]))


;; 
;; 
;; Subscriptions
;; 
;; 

(defmethod core/msgs! ::toast [{:keys [state! dispatch!]}]
  (go-loop []
    (when (not (nil? (-> @state! ::toast)))
      (dispatch! {:type ::time-passed}))
    (<! (timeout 1000))
    (recur)))