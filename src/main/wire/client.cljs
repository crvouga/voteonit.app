(ns wire.client
  (:require [core :refer [handle-eff! add-eff]]
            ["socket.io-client" :as socket-io]))

(defn send-to-server [input & msgs]
  (add-eff input {:type ::send-to-server :msgs msgs}))

(defmethod handle-eff! ::send-to-server [input]
  (print "send this to server", (pr-str (-> input :eff :msgs) ))
  (print (-> input :eff)))
  

(def is-localhost (= "localhost" (.-hostname js/window.location)))

(print "is-localhost" is-localhost)

(def server-url (if  is-localhost "http://localhost:3000" ""))

(defn start-web-socket! []
  (let [socket (socket-io/io server-url)]
    (.emit socket "client-connected")))