(ns db.storage.filesystem
  (:require [db.storage]
            [clojure.edn]
            [cljs.pprint]
            [datascript.core :as d]
            ["fs" :as fs]))

;; docs: https://github.com/tonsky/datascript/blob/master/docs/storage.md

;; 
;; 
;; 
;; 
;; 


(defn encode-db [db]
  ;; https://stackoverflow.com/questions/32107313/pretty-print-to-a-string-in-clojurescript
  (with-out-str (cljs.pprint/pprint db)))

(defn decode-db [encoded-db]
  (clojure.edn/read-string
   {:readers d/data-readers}
   encoded-db))

;; 
;; 
;; 
;; 
;; 

(def db-file-path "data/datascript_db.edn")

(defmethod db.storage/read-db! :filesystem [_] 
  (println "db.storage.filesystem/read-db")
  (try
    (let [db-content (fs/readFileSync db-file-path "utf-8")
          db         (decode-db db-content)]
      (println "Database read successfully")
      db)
    (catch js/Error e
      (println "Error reading the database" e))))

(defmethod db.storage/write-db! :filesystem [{:keys [conn!]}]
  (try
    (let [db-content (encode-db @conn!)]
      (fs/writeFileSync db-file-path db-content "utf-8")
      (println "Database written successfully"))
    (catch js/Error e
      (println "Error writing the database" e))))
