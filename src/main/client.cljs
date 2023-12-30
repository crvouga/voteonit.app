(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            [app.client]
            [core]))

(def state (r/atom {:model (app.client/init)}))

(defn dispatch! [msg]
  (reset! state (core/step {:model (-> @state :model) :msg msg})))

(add-watch state :run-effects 
           (fn [_ _ _ new-state]
             (let [new-effects (-> new-state :effects)]
               (doseq [eff new-effects] (eff dispatch!)))))

(defn view []
  [app.client/view 
   {:dispatch! dispatch! 
    :model (:model @state)}])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (rd/render [view] (js/document.getElementById "root")))

