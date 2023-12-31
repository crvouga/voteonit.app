(ns server.email
  (:require [core :refer [handle-effect! append-effect]]))


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
  [input {:keys [email]}]
  (append-effect input {:type :send-email
                     :provider :send-grid
                     :email email}))
  
(defmethod handle-effect! :send-email [input]
  (send-email! input))

