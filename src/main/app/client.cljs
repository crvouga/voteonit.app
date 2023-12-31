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

(defn view [{:keys [] :as input}] 
   [:div.w-screen.flex.flex-col.items-center.justify-center.bg-neutral-50.overflow-hidden
    {:style {:height "100dvh"}}
    [:div.flex.flex-col.gap-4.w-full.max-w-md
     [app.auth.client/view-login-page input]]])
  
