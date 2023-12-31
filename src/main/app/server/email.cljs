(ns app.server.email
  (:require [core :refer [handle-eff! add-eff]]))


;; 
;; 
;; 
;; 
;; 
;; 

(defmulti send-email! (fn [input] (-> input :provider)))

(defmethod send-email! :send-grid [input])

(defmethod send-email! :mail-gun [input])

(defmethod send-email! :default [input])
  
;; 
;; 
;; 
;; 
;; 

(defn send-email
  [input {:keys [email]}]
  (add-eff input {:type :send-email
                     :provider :send-grid
                     :email email}))
  
(defmethod handle-eff! :send-email [input]
  (send-email! input))

