(ns app.client
  (:require [app.auth.client]
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
   {}))


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
  
