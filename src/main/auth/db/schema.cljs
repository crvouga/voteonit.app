(ns auth.db.schema)

(def schema-user-accounts
  {:user-account/username {:db/cardinality :db.cardinality/one
                           :db/valueType   :db.type/string
                           :db/unique      :db.unique/identity
                           :db/fulltext    true}
   
   :user-account/email    {:db/cardinality :db.cardinality/one
                           :db/valueType   :db.type/string
                           :db/unique      :db.unique/identity
                           :db/fulltext    true}
   
   :user-account/created-at {:db/cardinality :db.cardinality/one
                             :db/valueType   :db.type/instant}})


(def schema-user-session
  {:user-session/user {:db/cardinality :db.cardinality/one
                       :db/valueType   :db.type/ref}
   
   :user-session/session-id {:db/cardinality :db.cardinality/one
                             :db/valueType   :db.type/string
                             :db/unique      :db.unique/identity}
   
   :user-session/created-at {:db/cardinality :db.cardinality/one
                             :db/valueType   :db.type/instant}})


(def schema (merge schema-user-accounts schema-user-session))