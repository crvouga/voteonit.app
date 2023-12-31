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

(defn assoc-guest-session [{:keys [state msg]}]
  (let [{:keys [session-id client-id]} msg
        new-guest-account {:user-id session-id}]
    (-> state
        (update ::session-id-by-client-id assoc client-id session-id)
        (update ::user-id-by-session-id dissoc session-id nil)
        (update ::session-ids conj session-id)
        (update ::accounts-by-user-id assoc session-id new-guest-account))))

(defn send-client-auth-state [input]
  (let [client-id (-> input :msg :client-id) 
        to-client {:type auth.core/client-auth-state
                    :user-id nil
                    :account nil}
        output (wire.server/send-to-client input client-id to-client)]
    output))

(defmethod handle-msg auth.core/user-clicked-continue-as-guest [input] 
   (-> input send-client-auth-state))

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti handle-event (fn [input] (-> input :msg :type)))

(register-event-handler! handle-event)

(defmethod handle-event :wire/client-connected [input] 
  (-> input send-client-auth-state))
  
