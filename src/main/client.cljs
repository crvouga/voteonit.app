(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            [ui.button]
            [auth.client]
            [core]))


(defn initial-state [] 
  (merge
   (auth.client/init)))

(defn view [{:keys [] :as input}] 
   [:div.w-screen.flex.flex-col.items-center.justify-center.bg-neutral-900.text-white.overflow-hidden
    {:style {:height "100dvh"}}
    [:div.flex.flex-col.gap-4.w-full.max-w-md
     [auth.client/view-login-page input]]])
  


;; 
;; 
;; 
;; 
;; 
;; 

(def state (r/atom {:state (initial-state)}))

(defn dispatch! [msg]
  (println msg)
  (reset! state (core/handle-msg {:state (-> @state :state) :msg msg})))

(add-watch state :run-effects (core/watch-handle-eff! dispatch!))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (rd/render [view {:dispatch! dispatch! :state (:state @state)}] (js/document.getElementById "root")))

