(ns db
  (:require [db.schema]
            [db.storage]
            [db.storage.filesystem]
            [datascript.core :as d]))

(def conn! 
  (d/create-conn db.schema/schema))