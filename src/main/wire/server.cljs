(ns wire.server
  (:require [core :refer [handle-eff! add-eff]]))

(defn send-to-client [input & msgs]
  (add-eff input {:type ::send-to-client :msgs msgs}))

(defn broadcast [input & msgs]
  (add-eff input {:type ::broadcast :msgs msgs}))

(defmethod handle-eff! ::send-to-client [input]
  (print (-> input :effect)))

(defmethod handle-eff! ::broadcast [input]
  (print (-> input :effect)))