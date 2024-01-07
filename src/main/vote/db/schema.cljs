(ns vote.db.schema)

(def schema-poll 
  {:poll/question {:db/cardinality :db.cardinality/one
                   :db/valueType   :db.type/string
                   :db/fulltext    true
                   :db/unique      :db.unique/identity}
   
   :poll/choices  {:db/cardinality :db.cardinality/many
                   :db/valueType   :db.type/ref}
   
   :poll/creator {:db/cardinality :db.cardinality/one
                  :db/valueType   :db.type/ref}
   
   :poll/created-at {:db/cardinality :db.cardinality/one
                     :db/valueType   :db.type/instant}})

(def schema-poll-choice
  {:poll-choice/poll {:db/cardinality :db.cardinality/one
                      :db/valueType   :db.type/ref}
   
   :poll-choice/choice {:db/cardinality :db.cardinality/one
                        :db/valueType   :db.type/string
                        :db/fulltext    true}})

(def schema-poll-choice-vote 
  {:poll-choice-vote/poll {:db/cardinality :db.cardinality/one
                           :db/valueType   :db.type/ref}  
   
   :poll-choice-vote/choice {:db/cardinality :db.cardinality/one
                             :db/valueType   :db.type/ref} 
   
   :poll-choice-vote/voter {:db/cardinality :db.cardinality/one
                            :db/valueType   :db.type/ref}
                
   :poll-choice-vote/voted-at {:db/cardinality :db.cardinality/one
                               :db/valueType   :db.type/instant}})

(def schema (merge schema-poll schema-poll-choice-vote schema-poll-choice))

