(ns app.client
  (:require [core :refer [step add-effect]]))

;; 
;; 
;; 
;; 
;; 
;; 

(defn init [] {::count 0})

;; 
;; 
;; 
;; 
;; 
;; 

(defn update-count [input f]
  (-> input (update-in [:model ::count] f)))

(defmethod step ::increment [input]
  (-> input (update-count inc)))

(defmethod step ::decrement [input]
  (-> input (update-count dec)))

(defmethod step ::say-hi [input]
  (-> input (add-effect (fn [] (println "Hi!")))))

;; 
;; 
;; 
;; 
;; 
;; 

(defn view [input]
  (let [{:keys [dispatch! model]} input]
    [:div
     [:ul
      [:li (str "Hello " (::count model) " times")]
      [:button {:on-click #(dispatch! {:t ::say-hi})} "Say hi"]
      [:button {:on-click #(dispatch! {:t ::increment})} "Increment"]
      [:button {:on-click #(dispatch! {:t ::decrement})} "Decrement"]]]))
  
