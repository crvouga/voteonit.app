(ns wire.client
  (:require [core :refer [handle-eff! add-eff]]))

(defn send-to-server [input & msgs]
  (add-eff input {:type ::send-to-server :msgs msgs}))

(defmethod handle-eff! ::send-to-server [input]
  (print "send this to server")
  (print (-> input :eff)))