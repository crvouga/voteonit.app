(ns app.auth.server 
  (:require [core :refer [step]]
            [app.auth.core]))

(defn init []
  {::session-id-by-client-id {}
   ::user-id-by-session-id {}
   ::session-ids #{}
   ::accounts-by-user-id {}})

(defn send-email
  [input {:keys [email]}]
  (println "send email to " email))

(defn make-login-email [email]
  (str "https://voteonit.app/login/" email))

(defn send-to-client [input to-client]
  (let [eff (fn [] (println "todo" to-client))
        output (core/add-effect input eff)]
    output))

(defmethod step app.auth.core/user-clicked-send-login-link-email [input]
  (let [email (-> input :msg :email)
        login-link-email (make-login-email email)
        sent-email (send-email input login-link-email)]
    sent-email))


