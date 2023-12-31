(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            [app.client]
            [core]))

(def state (r/atom {:state (app.client/init)}))

(defn dispatch! [msg]
  (println msg)
  (reset! state (core/handle-msg {:state (-> @state :state) :msg msg})))

(add-watch state :run-effects (core/watch-handle-eff! dispatch!))

(defn view []
  [app.client/view 
   {:dispatch! dispatch! 
    :state (:state @state)}])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (rd/render [view] (js/document.getElementById "root")))

