(ns wire.server
  (:require [core :refer [handle-effect! publish-event append-effect handle-msg]]
            ["socket.io" :as socket-io]
            [wire.core]
            [cljs.core.async :refer [chan put! <! go]]))

(defmethod handle-msg ::client-connected [input]
  (let [event (merge (:msg input) {:type :client-connected})]
    (publish-event input event)))

(defn send-to-client [input client-id & msgs]
  (append-effect input {:type ::send-to-client 
                        :client-id client-id 
                        :msgs msgs}))

(def to-client-msgs-chan (chan))

(defmethod handle-effect! ::send-to-client [input] 
  (put! to-client-msgs-chan (-> input :effect))
  input)

(defmethod handle-effect! ::broadcast [input]
  input)


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

(defn decode-msgs [encoded-msgs client-id session-id]
  (let [decoded (wire.core/edn-decode encoded-msgs)
        assoc-ids (fn [msg] (assoc msg :client-id client-id :session-id session-id))
        decoded-with-ids (map assoc-ids decoded)]
    decoded-with-ids))

(defn generate-client-id! []
  (str "client-id:" (rand-int 100000)))

(defn generate-session-id! []
  (str "session-id:" (rand-int 100000)))


(defn listen-to-sever-msgs! [^js socket dispatch! client-id session-id]
  (.on socket "to-server-msgs"
       (fn [encoded-msgs]
         (doseq [msg (decode-msgs encoded-msgs client-id session-id)]
           (dispatch! msg))))
  
  (dispatch! {:type ::client-connected 
              :client-id client-id 
              :session-id session-id}))


(defn on-connect [dispatch! ^js socket]
  (let [client-id (generate-client-id!)]
    
    (swap! sockets-by-client-id assoc client-id socket)
    
    (.on socket "session-id"
         (fn [session-id]
           (println "recieved session-id" session-id)
           (if (not (string? session-id))
             (.emit socket "session-id" (generate-session-id!))
             (listen-to-sever-msgs! socket dispatch! client-id session-id))))

    (.on socket "disconnect" #(on-disconnect client-id))))

    
(def socket-config {:cors {:origin "*"
                           :methods ["GET" "POST" "DELETE" "OPTIONS" "PUT" "PATCH"]
                           :credentials false}})

(defn subscriptions! [^js http-server dispatch!] 
  (let [io (new socket-io/Server http-server (clj->js socket-config))]
    (.on io "connection" #(on-connect dispatch! %))))