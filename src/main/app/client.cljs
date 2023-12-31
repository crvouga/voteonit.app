(ns app.client
  (:require [core :refer [step add-effect]]
            [app.auth.client]
            [ui.button]))

;; 
;; 
;; 
;; 
;; 
;; 

(defn init [] 
  (merge
   (app.auth.client/init)
   {::count 0}))

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

(def eff-say-hi (fn [] (println "Hi!")))

(defmethod step ::say-hi [input]
  (-> input (add-effect eff-say-hi)))

;; 
;; 
;; 
;; 
;; 
;; 

(defn view [input]
  (let [{:keys [dispatch! model]} input]
    [:div.w-full.flex.flex-col.items-center.justify-center
     {:style {:height "100dvh"}}
     [:div.flex.flex-col.gap-4.w-full.max-w-md
      [:pre (pr-str model)]
      [app.auth.client/view-login-page input]
      [:p.text-3xl.font-bold (str "Hello " (::count model) " times")]
      [ui.button/view {:text "Say hi" :on-click #(dispatch! {:type ::say-hi})}]
      [ui.button/view {:text "Increment" :on-click #(dispatch! {:type ::increment})}]
      [ui.button/view {:text "Decrement" :on-click #(dispatch! {:type ::decrement})}]]]))
  
