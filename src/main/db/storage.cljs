(ns db.storage)

;; 
;; 
;; 
;; 
;; 
;; 

(defn- dispatch [input]
  (-> input :storage))

(defmulti read-db! dispatch)

(defmulti write-db! dispatch)

;; 
;; 
;; 
;; 
;; 
;; 

(def default-storage :filesystem)

(defmethod read-db! :default [input]
  (read-db! (-> input (assoc :storage default-storage))))

(defmethod write-db! :default[input]
  (write-db! (-> input (assoc :storage default-storage))))