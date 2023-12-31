(ns wire.client
  (:require [core :refer [handle-eff! append-effect]]
            ["socket.io-client" :as socket-io]
            [wire.core]
            [cljs.core.async :refer [chan put! go <!]]))

(def to-server-msgs-chan (chan))
(def to-client-msgs-chan (chan))

(defn send-to-server [input & msgs]
  (append-effect input {:type ::send-to-server 
                        :msgs msgs}))

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
    (.on socket "to-client-msgs" 
         (fn [encoded-msgs]
           (let [msgs (wire.core/edn-decode encoded-msgs)]
             (put! to-client-msgs-chan msgs))))
    (go 
      (while true
        (let [msgs (<! to-server-msgs-chan)
              encoded-msgs (wire.core/edn-encode msgs)]
          (.emit socket "to-server-msgs" encoded-msgs))))))