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

(defmethod send-email! :default [input]
  (send-email! (assoc input :provider :send-grid)))
  
;; 
;; 
;; 
;; 
;; 

(defn send-email
  [input email]
  (core/add-eff input {:type ::send-email :email email}))
  
(defmethod core/on-eff! ::send-email [input]
  (send-email! input))

