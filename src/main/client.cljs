(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            [app.client]
            [core]))

(def state (r/atom {:model (app.client/init)}))

(defn dispatch! [msg]
  (println msg)
  (reset! state (core/step {:model (-> @state :model) :msg msg})))

(defn run-effects [_ _ _ new-state]
  (let [new-effects (-> new-state :effects)]
    (doseq [eff new-effects] 
      (eff dispatch!))))

(add-watch state :run-effects run-effects)

(defn view []
  [app.client/view 
   {:dispatch! dispatch! 
    :model (:model @state)}])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (rd/render [view] (js/document.getElementById "root")))

