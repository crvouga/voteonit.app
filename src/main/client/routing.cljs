(ns client.routing
  (:require [cljs.core.async :as async]
            [core.routing]
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
  {::current-route (core.routing/get-route!)})

(def path core.routing/path)

(defn ->current-route-path [input]
  (-> input ::current-route path))

;; 
;; 
;; 
;; 
;; 
;; 

(defn push-route [input route] 
  (core/add-eff input ::push-route {::new-route route}))

(defmethod core/on-eff! ::push-route [input] 
  (core.routing/push-route! (::new-route input))
  input)

(defn pop-route [input]
  (core/add-eff input ::pop-route {}))

(defmethod core/on-eff! ::pop-route [input]
  (core.routing/pop-route!)
  input)
  
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/on-msg ::new-route [input]
  (let [new-route (-> input ::new-route)]
    (-> input 
        (assoc ::current-route new-route))))
    


;; 
;; 
;; 
;; 
;; 
;; 

(defmulti view 
  (fn [input] (-> input ::current-route core.routing/path)))

;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/msgs! ::routing [{:keys [dispatch!]}]
  (core.routing/start-listening!)
  (async/go
    (while true
      (let [new-route (async/<! core.routing/route-chan!)]
        (dispatch! {core/msg ::new-route ::new-route new-route})))))