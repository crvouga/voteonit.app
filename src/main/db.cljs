(ns db
  (:require [db.schema]
            [datascript.core :as d]))


(def conn (d/create-conn db.schema/schema))