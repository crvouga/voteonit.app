(ns client.routing
  (:require [cljs.core.async :as async]
            [client.route]
            [ui.button]
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
  {::current-route (client.route/get-route!)})

(defn ->current-route-path [input]
  (-> input ::current-route client.route/route->path))

;; 
;; 
;; 
;; 
;; 
;; 

(defn push-route [input new-route]  
  (let [effect-added (core/add-eff input ::push {::route new-route})]
    effect-added))

(defmethod core/on-eff! ::push [input] 
  (let [new-route (-> input ::route)]
    (client.route/push-route! new-route)
    input))

;; 
;; 
;; 
;; 

(defn pop-route [input]
  (-> input
      (core/add-eff ::pop-route {})))

(defmethod core/on-eff! ::pop-route [input]
  (client.route/pop-route!)
  input)
  
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/on-msg ::current-route-changed [input]
  (let [route-new (-> input ::route)]
    (-> input 
        (assoc ::current-route route-new))))
    
(defn msgs-current-route-changed! [dispatch!]
  (async/go
    (while true
      (let [route (async/<! client.route/route-chan!)]
        (println "route changed" route)
        (dispatch! {core/msg ::current-route-changed ::route route})))))

;; 
;; 
;; 
;; 
;; 
;; 

(defn ->route [path payload]
  (client.route/->route path payload))

(defmulti view-route (fn [input] (-> input ::current-route client.route/route->path)))

;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/msgs! ::routing [{:keys [dispatch!]}]
  (msgs-current-route-changed! dispatch!)
  (client.route/start-listening!))