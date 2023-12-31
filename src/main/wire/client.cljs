(ns wire.client
  (:require [core :refer [handle-eff! append-effect]]
            ["socket.io-client" :as socket-io]
            [wire.core]
            [cljs.core.async :refer [chan put! go <!]]))

(defn send-to-server [input & msgs]
  (append-effect input {:type ::send-to-server :msgs msgs}))

(def to-server-msgs-chan (chan))

(defmethod handle-eff! ::send-to-server [input]
  (put! to-server-msgs-chan (-> input :eff :msgs))
  input)
  
;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 

(def is-localhost (= "localhost" (.-hostname js/window.location)))

(def server-url (if  is-localhost "http://localhost:3000" ""))

(defn attach-web-socket! []
  (let [socket (socket-io/io server-url)]
    (.emit socket "client-connected")
    (go
      (while true
        (let [msgs (<! to-server-msgs-chan)
              encoded-msgs (wire.core/edn-encode msgs)]
          (println "sending to server" encoded-msgs)
          (.emit socket "to-server-msgs" encoded-msgs))))))