(ns app.auth.server 
  (:require [core :refer [step]]
            [app.auth.core]))

(defn init []
  {::sessions {}})

(defmethod step app.auth.core/user-clicked-send-login-link-email [input]
  (println "server step" input)
  input)
