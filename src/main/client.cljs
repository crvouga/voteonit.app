(ns client
  (:require [auth.client]
            ["react-dom/client" :refer [createRoot]]
            [goog.dom :as gdom]
            [client.routing]
            [client.toast]
            [core]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [vote.client]))

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

(defonce root (createRoot (gdom/getElement "root")))

;; https://stackoverflow.com/questions/72389560/how-to-rerender-reagent-ui-with-react-18-and-shadow-cljs-reload

(defn view-main []
  [view (merge @state! {:dispatch! dispatch!})])

(defn init-view
  []
  (.render root (r/as-element [view-main])))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^:dev/after-load re-render []
  (init-view))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (core/init! state!)
  (core/msgs! {:state! state! :dispatch! dispatch!})
  (init-view))


