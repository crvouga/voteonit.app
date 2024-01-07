(ns vote.create.server
  (:require [core]
            [wire.server]
            [vote.create.core]
            [datascript.core :as d]))



(let [schema {:aka {:db/cardinality :db.cardinality/many
                    :db/valueType   :db.type/string}}
      conn   (d/create-conn schema)]
  (d/transact! conn [ { :db/id -1
                        :name  "Maksim"
                        :age   45
                        :aka   ["Max Otto von Stierlitz", "Jack Ryan"] } ])
  (d/q '[ :find  ?n ?a
          :where [?e :aka "Max Otto von Stierlitz"]
                 [?e :name ?n]
                 [?e :age  ?a] ]
       @conn))

(defmethod core/on-msg vote.create.core/create-poll [input]
  input)