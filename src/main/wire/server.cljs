(ns wire.server
  (:require [core]
            ["socket.io" :as socket-io]
            [wire.core]
            [cljs.core.async :refer [chan put! <! go]]))

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(core/register-module! ::wire)

(defmethod core/on-init ::wire []
  {::sockets-by-client-id {}})

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(def client-connected ::client-connected)

(defmethod core/on-msg ::client-connected [input]
  (core/add-evt input client-connected (select-keys input [:client-id :session-id])))

(defn send-to-client [input client-id & msgs]
  (core/add-eff input ::send-to-client {:client-id client-id :to-client-msgs msgs}))

(def to-client-msgs-chan! (chan))

(defmethod core/on-eff! ::send-to-client [input] 
  (put! to-client-msgs-chan! input)
  input)

(defmethod core/on-eff! ::broadcast [input]
  input)

;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/on-evt ::wire [input] input)

;; 
;; 
;; 
;; 
;; 
;; 
;; 


(def sockets-by-client-id! (atom {}))


;; 
;; Send messages to clients
;; 

(go
  (while true
    (let [to-client (<! to-client-msgs-chan!)
          client-id (-> to-client :client-id)
          to-client-msgs (-> to-client :to-client-msgs)
          to-client-msgs-encoded (wire.core/edn-encode to-client-msgs)
          socket (get @sockets-by-client-id! client-id)
          emit (fn [^js socket] 
                 (.emit socket "to-client-msgs" to-client-msgs-encoded))]
      (if socket
        (emit socket)
        (println "No socket for client-id" client-id)))))

;; 
;; 
;; 
;; 

(defn on-disconnect [client-id]
  (swap! sockets-by-client-id! dissoc client-id))

(defn decode-msgs [encoded-msgs client-id session-id]
  (let [decoded (wire.core/edn-decode encoded-msgs)
        assoc-ids #(assoc % :client-id client-id :session-id session-id)
        decoded-with-ids (map assoc-ids decoded)]
    decoded-with-ids))

(defn client-id! []
  (str "client-id:" (rand-int 100000)))

(defn session-id! []
  (str "session-id:" (rand-int 100000)))

(defn listen-to-sever-msgs! [^js socket dispatch! client-id session-id]
  (.on socket "to-server-msgs"
       (fn [encoded-msgs]
         (doseq [msg (decode-msgs encoded-msgs client-id session-id)]
           (dispatch! msg))))
  
  (dispatch! {core/msg ::client-connected 
              :client-id client-id 
              :session-id session-id}))


(defn on-connect! [dispatch! ^js socket!]
  (let [client-id (client-id!)]
    
    (swap! sockets-by-client-id! assoc client-id socket!)
    
    (.on socket! "session-id"
         (fn [session-id]
           (if (not (string? session-id))
             (.emit socket! "session-id" (session-id!))
             (listen-to-sever-msgs! socket! dispatch! client-id session-id))))

    (.on socket! "disconnect" #(on-disconnect client-id))))


(def socket-config {:cors {:origin "*"
                           :methods ["GET" "POST" "DELETE" "OPTIONS" "PUT" "PATCH"]
                           :credentials false}})

(defmethod core/msgs! ::wire [{:keys [^js http-server! dispatch!]}] 
  (let [io! (new socket-io/Server http-server! (clj->js socket-config))]
    (.on io! "connection" #(on-connect! dispatch! %))))