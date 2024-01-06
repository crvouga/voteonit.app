(ns auth.core)

(def user-clicked-send-login-link-email ::user-clicked-send-login-link-email)

(def email ::email)

(defn ->user-clicked-send-login-link-email [email]
  {:core/msg user-clicked-send-login-link-email
   ::email email})



(def user-clicked-continue-as-guest ::user-clicked-continue-as-guest)

(def user-clicked-logout-button ::user-clicked-logout-button)

(def user-logged-out ::user-logged-out)

(def user-logged-in ::user-logged-in)

(def current-user-account ::client-auth-account)
