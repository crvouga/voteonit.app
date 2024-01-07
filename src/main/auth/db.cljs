(ns auth.db
  (:require [datascript.core :as d]))

(def schema-user-accounts
  {:user-account/user-id  {:db/cardinality :db.cardinality/one
                           :db/unique      :db.unique/identity}
   
   :user-account/username {:db/cardinality :db.cardinality/one
                           :db/unique      :db.unique/identity
                           :db/fulltext    true}
   
   :user-account/email    {:db/cardinality :db.cardinality/one
                           :db/unique      :db.unique/identity
                           :db/fulltext    true}
   
   :user-account/created-at {:db/cardinality :db.cardinality/one}})


(def schema-user-session
  {:user-session/session-id {:db/cardinality :db.cardinality/one
                             :db/unique      :db.unique/identity}
   
   :user-session/user {:db/cardinality :db.cardinality/one
                       :db/valueType   :db.type/ref}
   
   :user-session/created-at {:db/cardinality :db.cardinality/one}})

(def schema 
  (merge schema-user-accounts schema-user-session))

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(def query-user-account-by-session-id
  '[:find [(pull ?user-account [*])]
    :in $ ?session-id
    :where
    [?user-session :user-session/session-id ?session-id]
    [?user-session :user-session/user ?user-account]])

(defn find-one-user-account-by-session-id [conn session-id] 
  (first (d/q query-user-account-by-session-id @conn session-id)))

(def query-session-entity-id
  '[:find ?e
    :in $ ?session-id
    :where
    [?e :user-session/session-id ?session-id]])

;; 
;; 
;; 
;; 
;; 
;; 

(defn user-id! []
  (str "user-id:" (rand-int 1000000)))

(defn guest-username! []
  (str "Guest " (rand-int 1000000)))

(defn guest-user-account! []
  (let [user-id (user-id!)
        username (guest-username!)]
    {:user-account/user-id user-id
     :user-account/username username
     :user-account/created-at (.now js/Date)}))

(defn add-guest-user-account! [conn user-account]
  (let [tx-data (merge {:db/add -1}
                       user-account)]
    (d/transact! conn [tx-data])))

;; 
;; 
;; 
;; 
;; 
;; 

(defn add-user-session! [conn input user-account]
  (let [user-ref {:user-account/user-id (-> user-account :user-account/user-id)}
        tx-data {:db/add -1
                 :user-session/user user-ref
                 :user-session/session-id (-> input :session-id)
                 :user-session/created-at (.now js/Date)}]
    (d/transact! conn [tx-data])))

(defn remove-user-session! [conn input]
  (let [session-id (-> input :session-id)
        session-entity-id (first (d/q query-session-entity-id @conn session-id))]
    (when session-entity-id
      (d/transact! conn [[:db/retractEntity (first session-entity-id)]]))))