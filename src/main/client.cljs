(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            [clojure.pprint :refer [pprint]]
            [ui.button]
            [ui.spinner]
            [auth.client]
            [client.toast]
            [client.routing]
            [wire.client]
            [vote.client]
            [core]))

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defmulti view-main auth.client/to-auth-state)

(defmethod view-main :logged-out [input]
  [auth.client/view-login-screen input])

(defmethod view-main :logged-in [input]
  [client.routing/view-route input])

(defmethod view-main :default []
  [ui.spinner/screen])

(defn view [input] 
   [:div.w-screen.flex.flex-col.items-center.justify-center.bg-neutral-900.text-white.overflow-hidden
    {:style {:height "100dvh"}}
    [:div.flex.flex-col.gap-4.w-full.max-w-md.h-full.relative 
     [:pre (pprint input)]
     [client.toast/view input]
     [view-main input]]])

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(def state! (r/atom (core/initial-state)))

(defn dispatch! [msg]
  (let [stepped (core/step! (merge @state! {:msg msg}))]
    (reset! state! stepped)))

(defn root-view [] 
  [view (merge @state! {:dispatch! dispatch!})])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (core/subscriptions! {:state! state! :dispatch! dispatch!})
  (rd/render [root-view] (js/document.getElementById "root")))