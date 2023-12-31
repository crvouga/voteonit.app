(ns auth.core)

(defn user-clicked-send-login-link-email []
  {:type ::user-clicked-send-login-link-email})

(defn user-clicked-continue-as-guest [] 
  {:type ::user-clicked-continue-as-guest})

(def client-auth-state ::client-auth-state)
