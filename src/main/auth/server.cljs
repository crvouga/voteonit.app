(ns auth.server 
  (:require [core]
            [auth.core]
            [wire.server]
            [server.email]
            [db]
            [auth.db]))

;; 
;; 
;; 
;; 
;; 
;; 
;; 


(core/register-module! ::auth)


;; 
;; 
;; 
;; 
;; 
;; 


(defmethod core/on-init ::auth []
  {})

;; 
;; 
;; 
;; 
;; 
;; 

(defn send-logged-in [input]
  (let [user-account (auth.db/find-one-user-account-by-session-id db/conn! (-> input :session-id))
        client-id (-> input :client-id)
        to-client {core/msg auth.core/user-logged-in :user-account user-account}]
    (-> input
        (wire.server/send-to-client client-id to-client))))

(defn add-new-guest-session! [input]
  (let [guest-user-account (auth.db/guest-user-account!)]
    (auth.db/add-guest-user-account! db/conn! guest-user-account)
    (auth.db/add-user-session! db/conn! input guest-user-account)
    input)) 

(defmethod core/on-msg auth.core/user-clicked-continue-as-guest [input]
  (let [user-account (auth.db/find-one-user-account-by-session-id db/conn! (-> input :session-id))]
    (if user-account
      (send-logged-in input)
      (-> input add-new-guest-session! send-logged-in))))

;; 
;; 
;; 
;; 
;; 
;; 

(defmethod 
  core/on-msg 
  auth.core/user-clicked-send-login-link-email 
  [input] 
  input)


;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 


(defn remove-session! [input]
  (auth.db/remove-user-session! db/conn! input)
  input)

(defmethod core/on-msg auth.core/user-clicked-logout-button [input] 
  (let [client-id (-> input :client-id)
        to-client {core/msg auth.core/user-logged-out}]
    (-> input
        remove-session!
        (wire.server/send-to-client client-id to-client))))

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti on-evt core/evt)

(defmethod on-evt :default [input] input)

(defmethod core/on-evt ::auth [input]
  (on-evt input))

(defmethod on-evt wire.server/client-connected [input]
  (let [client-id (-> input :client-id)
        user-account (auth.db/find-one-user-account-by-session-id db/conn! (-> input :session-id))
        to-client {core/msg auth.core/current-user-account :user-account user-account}] 
    (-> input
        (wire.server/send-to-client client-id to-client))))


;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/msgs! ::auth [])