(ns wire.server
  (:require [core :refer [handle-eff! add-eff]]))

(defn send-to-client [input & msgs]
  (add-eff input {:type ::send-to-client :msgs msgs}))

(defmethod handle-eff! ::send-to-client [input]
  (print (-> input :eff)))

(defmethod handle-eff! ::broadcast [input]
  (print (-> input :eff)))


;; 
;; 
;; 
;; 
;; 
;; 
;; 

