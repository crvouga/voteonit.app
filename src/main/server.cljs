(ns server
  (:require [core]))

(def state (atom {:model {}}))

(defn dispatch! [msg]
  (println msg)
  (reset! state (core/step {:model (-> @state :model) :msg msg})))

(add-watch state :run-effects (core/run-effects! dispatch!))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (println "Hello from ClojureScript!"))

