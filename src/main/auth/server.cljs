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


(defn initial-state []
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
  (let [session-id (-> input :msg :session-id)
        user-id (-> input ::user-id-by-session-id (get session-id))
        account (-> input ::accounts-by-user-id (get user-id))]
    account))

(defn send-logged-in [input]
  (let [account (to-user-account input)
        client-id (-> input :msg :client-id)
        to-client {:type auth.core/user-logged-in :account account}]
    (wire.server/send-to-client input client-id to-client)))

;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/handle-msg auth.core/user-clicked-send-login-link-email [input] 
  (let [login-link-email (make-login-login-email  (-> input :msg :email))
        sent-email (server.email/send-email input login-link-email)]
    sent-email))



(defmethod core/handle-msg auth.core/user-clicked-continue-as-guest [input]
  (-> input 
      assoc-new-guest-session 
      send-logged-in))

(defmethod core/handle-msg auth.core/user-clicked-logout-button [input] 
  (let [client-id (-> input :msg :client-id)]
    (-> input
        dissoc-session
        (wire.server/send-to-client client-id {:type auth.core/user-logged-out}))))


;; 
;; 
;; 
;; 
;; 
;; 

(defmulti handle-event (fn [input] (-> input :msg :type)))

(defn send-client-auth-state [input]
  (let [client-id (-> input :msg :client-id)
        account (to-user-account input)
        to-client {:type auth.core/current-user-account :account account}] 
    (wire.server/send-to-client input client-id to-client)))

(defmethod handle-event wire.server/client-connected [input] 
  (-> input send-client-auth-state))

(defmethod handle-event :default [input]
  (println "Unhandled event" (-> input :msg :type))
  input)
  
(core/register-event-handler! handle-event)



