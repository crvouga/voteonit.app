(ns client.routing
  (:require [cljs.core.async :as async]
            [client.route]
            [clojure.string]
            [clojure.edn]
            [core]))
            

;; 
;; 
;; 
;; 
;; 
;; 

(core/register-module! ::routing)

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/on-init ::routing []
  {::current-route nil})

(defn to-current-route [input]
  (-> input ::current-route))

;; 
;; 
;; 
;; 
;; 
;; 

(defn push-route [input route] 
  (-> input
      (assoc ::current-route route)
      (core/add-eff ::push :route route)))

(defmethod core/on-eff! ::push [input]
  (let [new-route (-> input core/eff :route)]
    (client.route/push-route! new-route)
    input))

;; 
;; 
;; 
;; 

(defn pop-route [input]
  (-> input
      (core/add-eff {:type ::pop-route})))

(defmethod core/on-eff! ::pop-route [input]
  (client.route/pop-route!)
  input)
  


;; 
;; 
;; 
;; 
;; 
;; 

(defn- current-route-change [route]
  {:type ::current-route-changed
   ::route route})

(defmethod core/on-msg ::current-route-changed [input]
  (let [route-new (-> input core/msg ::route)]
    (-> input 
        (assoc ::current-route route-new))))
    
(defn msgs-current-route-changed! [dispatch!]
  (async/go
    (while true
      (let [route (async/<! client.route/route-chan!)
            msg (current-route-change route)]
        (println "route changed" route)
        (dispatch! msg)))))

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti view-route (fn [input] (-> input ::current-route :type)))

(defmethod view-route :default [_input]
  [:div (str "Page not found")])

;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/msgs! ::routing [{:keys [dispatch!]}]
  (msgs-current-route-changed! dispatch!))