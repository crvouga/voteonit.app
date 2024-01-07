(ns auth.server 
  (:require [core]
            [auth.core]
            [wire.server]
            [server.email]))

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
  {::session-id-by-client-id {}
   ::user-id-by-session-id {}
   ::session-ids #{}
   ::accounts-by-user-id {}})


;; 
;; 
;; 
;; 
;; 
;; 

(defn make-login-login-email [email]
  {:email email})
  

(defn generate-user-id! []
  (str "user-id:" (rand-int 1000000)))

(defn generate-guest-account []
  (let [user-id (generate-user-id!)]
    {:user-id user-id
     :username (str "Guest " user-id)}))

(defn assoc-session [input user-account]
  (let [{:keys [msg]} input
        {:keys [client-id session-id]} msg
        {:keys [user-id]} user-account]
    (-> input
        (assoc-in [::session-id-by-client-id client-id] session-id)
        (update ::session-ids conj session-id)
        (assoc-in [::user-id-by-session-id  session-id] user-id)
        (assoc-in [::accounts-by-user-id user-id] user-account))))

(defn dissoc-session [input]
  (let [{:keys [msg]} input
        {:keys [client-id session-id]} msg]
    (-> input
        (update ::session-id-by-client-id dissoc client-id)
        (update ::session-ids disj session-id)
        (update ::user-id-by-session-id dissoc session-id))))

(defn assoc-new-guest-session [input]
  (let [guest-account (generate-guest-account)]
    (assoc-session input guest-account)))


(defn to-user-account [input]
  (let [session-id (-> input :session-id)
        user-id (-> input ::user-id-by-session-id (get session-id))
        account (-> input ::accounts-by-user-id (get user-id))]
    account))

(defn send-logged-in [input]
  (let [account (to-user-account input)
        client-id (-> input :client-id)
        to-client {core/msg auth.core/user-logged-in :account account}]
    (wire.server/send-to-client input client-id to-client)))

;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/on-msg auth.core/user-clicked-send-login-link-email [input] 
  (let [login-link-email (make-login-login-email  (-> input :email))
        sent-email (server.email/send-email input login-link-email)]
    sent-email))


(defmethod core/on-msg auth.core/user-clicked-continue-as-guest [input]
  (-> input 
      assoc-new-guest-session 
      send-logged-in))

(defmethod core/on-msg auth.core/user-clicked-logout-button [input] 
  (let [client-id (-> input :client-id)]
    (-> input
        dissoc-session
        (wire.server/send-to-client client-id {core/msg auth.core/user-logged-out}))))

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
        account (to-user-account input)
        to-client {core/msg auth.core/current-user-account :account account}] 
    (wire.server/send-to-client input client-id to-client)))


;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/msgs! ::auth [])