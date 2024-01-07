(ns client.toast
  (:require [core]
            [cljs.core.async :refer [go-loop timeout <!]]
            ["@headlessui/react" :as headlessui]))

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
  {::toast nil
   ::visible? false
   ::running-toast-id 0})

;; 
;; 
;; Toast
;; 
;; 

(defn ->toast [toast-id message]
  {:id toast-id
   :message message
   :duration 1500
   :created-at (js/Date.now)})

(defn toast-expired? [toast]
  (let [duration (-> toast :duration)
        created-at (-> toast :created-at)
        now (js/Date.now)
        elapsed (- now created-at)]
    (>= elapsed duration)))

;; 
;; 
;; cmd
;; 
;; 

(defn show-toast [input message]
  (core/add-cmd input {core/cmd ::show-toast :message message}))

(defmethod core/on-cmd ::show-toast [input] 
  (let [message (-> input :message)
        toast-id (-> input ::running-toast-id)
        new-toast (->toast toast-id message)]
    (-> input 
        (assoc ::toast new-toast)
        (update ::running-toast-id inc)
        (assoc ::visible? true))))

;; 
;; 
;; Msg
;; 
;; 

(defmethod core/on-msg ::time-passed [input]
  (let [toast (-> input ::toast)
        visible? (and toast (not (toast-expired? toast)))] 
    (-> input
      (assoc ::visible? visible?))))
  

;; 
;; 
;; View
;; 
;; 


(defn view [input] 
  [:div.absolute.inset-0.flex.items-start.justify-center.pointer-events-none.p-4.w-full
   [:> headlessui/Transition
    {:show (-> input ::visible?)
     :class-name "w-full"
     :enter "transition ease-out duration-300"
     :enter-from "opacity-0 -translate-y-full"  
     :enter-to "opacity-100 translate-y-0"     
     :leave "transition ease-in duration-200"
     :leave-from "opacity-100 translate-y-0"    
     :leave-to "opacity-0 -translate-y-full"}
    ^{:key (-> input ::toast :id)}
    [:div.w-full.max-w-md.bg-white.shadow-lg.rounded-lg.pointer-events-auto.ring-1.ring-black.ring-opacity-5.overflow-hidden.text-black.px-4.py-3
     (-> input ::toast :message)]]])



;; 
;; 
;; Subscriptions
;; 
;; 

(defmethod core/msgs! ::toast [{:keys [state! dispatch!]}]
  (go-loop []
    (when (-> @state! ::visible?)
      (dispatch! {core/msg ::time-passed}))
    (<! (timeout 500))
    (recur)))