(ns server.email
  (:require [core]))


;; 
;; 
;; 
;; 
;; 
;; 

(defmulti send-email! (fn [input] (-> input :provider)))

(defmethod send-email! :send-grid [])

(defmethod send-email! :mail-gun [])

(defmethod send-email! :default [])
  
;; 
;; 
;; 
;; 
;; 

(defn send-email
  [input]
  (core/add-eff input {:type :send-email
                       :provider :send-grid
                       :email (-> input core/eff :email)}))
  
(defmethod core/on-eff! :send-email [input]
  (send-email! input))

