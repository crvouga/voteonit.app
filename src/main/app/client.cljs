(ns app.client
  (:require [core :refer [step ->output]]))

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

(defmethod step ::increment [input]
  (-> input 
      ->output
      (assoc-in [:model ::count] (inc (-> input :model ::count)))))

;; 
;; 
;; 
;; 
;; 
;; 

(defn view [{:keys [dispatch! model]}]
  [:div
   [:pre (pr-str model)]
   [:ul
    [:li "Hello"]
    [:button {:on-click #(dispatch! {:type ::increment})} (str "Count: " (-> model ::count))]
    [:li {:style {:color "red"}} "World!"]]])

