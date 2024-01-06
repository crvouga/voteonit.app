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
      (core/add-eff ::push {:route route})))

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

(defn- current-route-change [route]
  {core/msg ::current-route-changed
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
        (dispatch! msg)))))

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti view-route (fn [input] (-> input ::current-route :type)))


;; 
;; 
;; 
;; 
;; 

(defmethod core/on-msg ::clicked-go-home-button [input]
  (push-route input :home-route))

(defmethod view-route :default [{:keys [dispatch!]}]
  [:div 
   [ui.button/view {:text "Go Home" :on-click #(dispatch! {core/msg ::clicked-go-home-button})}]])

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