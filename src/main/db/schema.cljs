(ns db.schema
  (:require [vote.db]
            [auth.db]))

(def schema 
  (merge 
   vote.db/schema 
   auth.db/schema))