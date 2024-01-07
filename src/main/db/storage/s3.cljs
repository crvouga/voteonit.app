(ns db.storage.s3
  (:require [db.storage]))

(defmethod db.storage/read-db! :aws-s3 [input]
  (println "db.storage.s3/read-db" input))

(defmethod db.storage/write-db! :aws-s3 [input]
  (println "db.storage.s3/write-db" input))