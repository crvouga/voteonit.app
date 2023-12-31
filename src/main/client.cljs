(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            [ui.button]
            [auth.client]
            [wire.client]
            [core]))

;; 
;; 
;; 
;; State
;; 
;; 
;; 

(defn initial-state [] 
  (merge
   (auth.client/initial-state)))

;; 
;; 
;; 
;; View
;; 
;; 
;; 

  
(defmulti view-main auth.client/to-auth-state)

(defmethod view-main :logged-out [input]
  [auth.client/view-login-page input])

(defmethod view-main :logged-in [input]
  [:div "logged in"])

(defn view [{:keys [] :as input}] 
   [:div.w-screen.flex.flex-col.items-center.justify-center.bg-neutral-900.text-white.overflow-hidden
    {:style {:height "100dvh"}}
    [:div.flex.flex-col.gap-4.w-full.max-w-md.h-full
     [view-main input]]])



;; 
;; 
;; 
;; 
;; 
;; 

(def state (r/atom (initial-state)))

(defn dispatch! [msg]
  (let [stepped (core/step! {:state @state :msg msg})]
    (reset! state (-> stepped :state))))

(defn root-view [] 
  [view {:state @state
         :dispatch! dispatch!}])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (wire.client/subscriptions! dispatch!)
  (rd/render [root-view] (js/document.getElementById "root")))