(ns auth.server 
  (:require [core :refer [handle-msg register-event-handler!]]
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
  

;; 
;; 
;; 
;; 
;; 
;; 

(defmethod handle-msg auth.core/user-clicked-send-login-link-email [input]
  (let [email (-> input :msg :email)
        login-link-email (make-login-login-email email)
        sent-email (server.email/send-email input login-link-email)]
    sent-email))

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
        (update-in [:state ::session-id-by-client-id] assoc client-id session-id)
        (update-in [:state ::session-ids] conj session-id)
        (update-in [:state ::user-id-by-session-id] assoc session-id user-id)
        (update-in [:state ::accounts-by-user-id] assoc user-id user-account))))

(defn dissoc-session [input]
  (let [{:keys [msg]} input
        {:keys [client-id session-id]} msg]
    (-> input
        (update-in [:state ::session-id-by-client-id] dissoc client-id)
        (update-in [:state ::session-ids] disj session-id)
        (update-in [:state ::user-id-by-session-id] dissoc session-id))))

(defn assoc-new-guest-session [input]
  (let [guest-account (generate-guest-account)]
    (assoc-session input guest-account)))

(defn send-client-auth-state [input]
  (let [session-id (-> input :msg :session-id)
        client-id (-> input :msg :client-id)
        user-id (-> input :state ::user-id-by-session-id (get session-id))
        account (-> input :state ::accounts-by-user-id (get user-id))
        to-client {:type auth.core/current-user-account :account account}
        output (wire.server/send-to-client input client-id to-client)]
    output))

(defmethod handle-msg auth.core/user-clicked-continue-as-guest [input]
   (-> input 
       assoc-new-guest-session 
       send-client-auth-state))

(defmethod handle-msg auth.core/user-clicked-logout-button [input]
  (-> input
      dissoc-session
      send-client-auth-state))

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti handle-event (fn [input] (-> input :msg :type)))

(defmethod handle-event :client-connected [input] 
  (-> input 
      send-client-auth-state))

(defmethod handle-event :default [input]
  (println "Unhandled event" (-> input :msg :type))
  input)
  
(register-event-handler! handle-event)