(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            [client.toast]
            [client.routing]
            [vote.client]
            [auth.client]
            [core]))

;; 
;; 
;; 
;; 
;; 
;; 
;; 


(defn view [input] 
   [:div.w-screen.flex.flex-col.items-center.justify-center.bg-neutral-900.text-white.overflow-hidden
    {:style {:height "100dvh"}}
    [:div.flex.flex-col.gap-4.w-full.max-w-md.h-full.relative 
     [client.toast/view input]
     [client.routing/view-route input]]])

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(def state! (r/atom (core/on-init)))

(defn dispatch! [msg]
  (core/step! state! msg))

(defn root-view [] 
  [view (merge @state! {:dispatch! dispatch!})])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (core/msgs! {:state! state! :dispatch! dispatch!})
  (rd/render [root-view] (js/document.getElementById "root")))