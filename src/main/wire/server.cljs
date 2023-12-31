(ns wire.server
  (:require [core :refer [handle-eff! append-effect]]
            ["socket.io" :as socket-io]
            [wire.core]
            [cljs.core.async :refer [chan put! <! go]]))

(def to-server-msgs-chan (chan))
(def to-client-msgs-chan (chan))

(defn send-to-client [input client-id & msgs]
  (append-effect input {:type ::send-to-client 
                        :client-id client-id 
                        :msgs msgs}))

(defmethod handle-eff! ::send-to-client [input] 
  (put! to-client-msgs-chan (-> input :eff))
  input)

(defmethod handle-eff! ::broadcast [input]
  (print (-> input :eff)))


;; 
;; 
;; 
;; 
;; 
;; 
;; 



(defn generate-client-id! []
  (str "client-id:" (rand-int 100000)))

(def sockets-by-client-id (atom {}))

(defn on-disconnect [client-id]
  (swap! sockets-by-client-id dissoc client-id))

(defn assoc-client-id [msgs client-id]
  (map (fn [msg] (assoc msg :client-id client-id)) msgs))

(go
  (while true
    (let [to-client (<! to-client-msgs-chan)
          client-id (-> to-client :client-id)
          msgs (-> to-client :msgs)
          encoded-msgs (wire.core/edn-encode msgs)
          socket (get @sockets-by-client-id client-id)]
      (when socket
        (.emit socket "to-client-msgs" encoded-msgs)))))

(defn on-connect [^js socket]
  (let [client-id (generate-client-id!)] 
    (swap! sockets-by-client-id assoc client-id socket)

    (.on socket "to-server-msgs" 
         (fn [encoded-msgs]
           (let [msgs (wire.core/edn-decode encoded-msgs)
                 msgs-with-ids (assoc-client-id msgs client-id)]
             (println "received from client" msgs-with-ids)
             (put! to-server-msgs-chan msgs-with-ids))))
    
    (.on socket "disconnect" #(on-disconnect client-id))))


(def socket-config {:cors {:origin "*"
                           :methods ["GET" "POST" "DELETE" "OPTIONS" "PUT" "PATCH"]
                           :credentials false}})

(defn attach-web-sockets! [^js http-server]
  (let [io (new socket-io/Server http-server (clj->js socket-config))]
    (.on io "connection" on-connect)))

