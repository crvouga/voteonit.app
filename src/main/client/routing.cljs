(ns client.routing
  (:require [cljs.core.async :refer [<! chan go put!]]
            [client.location]
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

(defmethod core/initial-state ::routing []
  {::stack []})

(defn to-current-route [input]
  (-> input ::stack last))

(defn push-stack [input route]
  (update input ::stack conj route))

(defn pop-stack [input]
  (update input ::stack pop))

(defn replace-stack [input route]
  (-> input 
      (pop-stack)
      (push-stack route)))


;; 
;; 
;; 
;; 
;; 
;; 

(defn- current-route-change [route]
  {:type ::current-route-changed
   :route route})

(defmethod core/handle-msg ::current-route-changed [input]
  (let [route (-> input :msg :route)]
    (-> input (replace-stack route))))


;; 
;; 
;; 
;; 
;; 

(def routes 
  {"/" {:type :vote}
   "/login" {:type :login}
   "/account" {:type :account}})

(defn pathname->route [pathname]
  (get routes pathname {:type :vote}))

(defn route->pathname [route]
  (or (first (filter #(= route (second %)) routes)) "/"))

;; 
;; 
;; 
;; 
;; 
;; 

(defn push-route [input route] 
  (-> input
      (core/append-effect {:type ::push :route route})
      (push-stack route)))

(defn- push! [route]
  (js/history.pushState nil nil (route->pathname route)))

(defmethod core/handle-effect! ::push [input]
  (let [route (-> input :effect :route)]
    (push! route)
    input))


;; 
;; 
;; 
;; 
;; 

(defn pop-route [input]
  (-> input
      (core/append-effect {:type ::pop-route})
      (pop-stack)))

(defmethod core/handle-effect! ::pop-route [input]
  (js/history.back)
  input)
  

;; 
;; 
;; 
;; 
;; 
;; 


(defmulti location->route (fn [input] (-> input :location :pathname)))

(defmulti route->location (fn [input] (-> input :route :type)))

(defmulti view-route (fn [input] (-> input to-current-route :type)))

(defmethod view-route :default [_input]
  [:div (str "Page not found")])

;; 
;; 
;; 
;; 
;; 
;; 

(defn- get-route! []
  (pathname->route (.-pathname js/window.location)))

(def route-chan (chan))

(defn put-route! []
  (let [route (get-route!)]
    (println route)
    (when route 
      (put! route-chan route))))
    
(defn dispatch-route-changes! [dispatch!]
  (go
    (while true
      (let [route (<! route-chan)
            msg (current-route-change route)]
        (when route
          (dispatch! msg))))))

(defmethod core/subscriptions! ::routing [{:keys [dispatch!]}]
  (.addEventListener js/window "hashchange" put-route!)
  (dispatch-route-changes! dispatch!)
  (put-route!))