(ns wire.server
  (:require [core :refer [handle-eff! append-effect]]
            ["socket.io" :as socket-io]
            [wire.core]
            [cljs.core.async :refer [chan put! <! go]]))

(defn send-to-client [input client-id & msgs]
  (append-effect input {:type ::send-to-client 
                        :client-id client-id 
                        :msgs msgs}))

(def to-client-msgs-chan (chan))

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


(def sockets-by-client-id (atom {}))


;; 
;; Send messages to clients
;; 

(go
  (while true
    (let [to-client (<! to-client-msgs-chan)
          client-id (-> to-client :client-id)
          msgs (-> to-client :msgs)
          encoded-msgs (wire.core/edn-encode msgs)
          socket (get @sockets-by-client-id client-id)
          emit (fn [^js socket] (.emit socket "to-client-msgs" encoded-msgs))]
      (when socket
        (emit socket)))))

;; 
;; 
;; 
;; 

(defn on-disconnect [client-id]
  (swap! sockets-by-client-id dissoc client-id))

(defn decode-msgs [encoded-msgs client-id]
  (let [decoded (wire.core/edn-decode encoded-msgs)
        decoded-with-client-id (map #(assoc % :client-id client-id) decoded)]
    decoded-with-client-id))

(defn generate-client-id! []
  (str "client-id:" (rand-int 100000)))

(defn on-connect [dispatch! ^js socket]
  (let [client-id (generate-client-id!)]
    (swap! sockets-by-client-id assoc client-id socket)
    
    (.on socket "disconnect" #(on-disconnect client-id))

    (.on socket "to-server-msgs"
         (fn [encoded-msgs]
           (doseq [msg (decode-msgs encoded-msgs client-id)]
             (dispatch! msg))))))
    
    
(def socket-config {:cors {:origin "*"
                           :methods ["GET" "POST" "DELETE" "OPTIONS" "PUT" "PATCH"]
                           :credentials false}})

(defn subscriptions! [^js http-server dispatch!] 
  (let [io (new socket-io/Server http-server (clj->js socket-config))]
    (.on io "connection" #(on-connect dispatch! %))))