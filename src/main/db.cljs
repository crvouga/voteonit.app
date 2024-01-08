(ns db
  (:require [db.schema]
            [db.storage]
            [db.storage.filesystem]
            [datascript.core :as d]))


(def empty-db (d/empty-db db.schema/schema))

#_(defn ensure-db [db]
  (if (d/db? db) db empty-db))

#_(def db-stored 
  (ensure-db (db.storage/read-db! {})))

(def conn! 
  (d/conn-from-db empty-db))

(d/transact! conn! [{:db/ident :db/schema 
                     :db.schema/schema db.schema/schema}])

(db.storage/write-db! {:conn! conn!})

(defn on-shutdown []
  (db.storage/write-db! {:conn! conn!})
  (js/process.exit 0))

(do
  (.on js/process "SIGINT" on-shutdown)   
  (.on js/process "SIGTERM" on-shutdown)  
  (.on js/process "SIGQUIT" on-shutdown)) 