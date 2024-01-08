(ns db
  (:require [db.schema]
            [db.storage]
            [db.storage.filesystem]
            [datascript.core :as d]))

;; https://cljdoc.org/d/datascript/datascript/1.5.0/api/datascript.core#empty-db

(defn ensure-db [db]
  (if (d/db? db) db (d/empty-db)))

(def db-stored 
  (ensure-db (db.storage/read-db! {})))

(def conn! 
  (d/conn-from-db db-stored))

(db.storage/write-db! {:conn! conn!})

(defn on-shutdown []
  (db.storage/write-db! {:conn! conn!})
  (js/process.exit 0))

(do
  (.on js/process "SIGINT" on-shutdown)   
  (.on js/process "SIGTERM" on-shutdown)  
  (.on js/process "SIGQUIT" on-shutdown)) 