(ns db.schema
  (:require [vote.db.schema]
            [auth.db.schema]))

(def schema (merge vote.db.schema/schema auth.db.schema/schema))