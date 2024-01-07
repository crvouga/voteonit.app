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


(def default-route {path nil})

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

(def route-changed ::route-changed)

(defmethod core/on-msg ::new-route [input]
  (let [new-current-route (-> input ::new-route)]
    (-> input 
        (assoc ::current-route new-current-route)
        (core/add-evt route-changed {::route new-current-route}))))
    


;; 
;; 
;; 
;; 
;; 
;; 

(defmulti view-path 
  (fn [input] (-> input ::current-route path)))

(defmethod core/on-msg ::clicked-go-home-button [input]
  (-> input (push-route default-route)))

(defmethod view-path :default [{:keys [dispatch!]}]
  [:div.w-full.h-full.flex.flex-col.justify-center.items-center.gap-6.p-6 
   [:p.text-4xl.font-bold.text-left.w-full "Page not found"]
   [ui.button/view 
    {:text "Go Home" 
     :on-click #(dispatch! {core/msg ::clicked-go-home-button})}]])

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