(ns app.auth.server 
  (:require [core :refer [handle-msg register-event-handler!]]
            [app.auth.core]
            [app.wire.server]
            [app.server.email]))


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

(defmethod handle-msg app.auth.core/user-clicked-send-login-link-email [input]
  (let [email (-> input :msg :email)
        login-link-email (make-login-login-email email)
        sent-email (app.server.email/send-email input login-link-email)]
    sent-email))

(defn assoc-guest-session [{:keys [model msg]}]
  (let [{:keys [session-id client-id]} msg
        new-guest-account {:user-id session-id}]
    (-> model
        (update ::session-id-by-client-id assoc client-id session-id)
        (update ::user-id-by-session-id dissoc session-id nil)
        (update ::session-ids conj session-id)
        (update ::accounts-by-user-id assoc session-id new-guest-account))))

(defn send-client-auth-state [input ]
  (let [client-id (-> input :msg :client-id)
        state (-> input :state)
        session-id (-> state ::session-id-by-client-id client-id)
        user-id (-> state ::user-id-by-session-id session-id)
        account (-> state ::accounts-by-user-id user-id)
        auth-state {:type app.auth.core/client-auth-state
                    :session-id session-id
                    :user-id user-id
                    :account account}
        output (app.wire.server/send-to-client input client-id auth-state)]
    output))

(defmethod handle-msg app.auth.core/user-clicked-continue-as-guest [input] 
   (-> input assoc-guest-session send-client-auth-state))

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
  
