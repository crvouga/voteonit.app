(ns wire.server
  (:require [core :refer [handle-eff! append-effect]]
            ["socket.io" :as socket-io]
            [wire.core]
            [cljs.core.async :refer [chan put! go <!]]))

(defn send-to-client [input & msgs]
  (append-effect input {:type ::send-to-client :msgs msgs}))

(defmethod handle-eff! ::send-to-client [input]
  (print "send this to client", (pr-str (-> input :eff :msgs) ))
  (print (-> input :eff)))

(defmethod handle-eff! ::broadcast [input]
  (print (-> input :eff)))


;; 
;; 
;; 
;; 
;; 
;; 
;; 

(def to-server-msgs-chan (chan))

(defn generate-client-id! []
  (str "client-id:" (rand-int 100000)))

(def sockets (atom {}))

(def socket-config {:cors {:origin "*"
                           :methods ["GET" "POST" "DELETE" "OPTIONS" "PUT" "PATCH"]
                           :credentials false}})

(defn on-disconnect [^js socket]
  (println "socket disconnected" socket)
  (swap! sockets dissoc socket))

(defn on-connect [^js socket]
  (let [client-id (generate-client-id!)] 
    (println "socket connected" client-id socket)
    
    (.on socket "to-server-msgs" 
         (fn [encoded-msgs]
           (let [msgs (wire.core/edn-decode encoded-msgs)]
             (println "received from client" msgs)
             (put! to-server-msgs-chan msgs))))
    
    (.on socket "disconnect" #(on-disconnect socket))))

(defn attach-web-sockets! [^js http-server]
  (let [io (new socket-io/Server http-server (clj->js socket-config))]
    (.on io "connection" on-connect)))

