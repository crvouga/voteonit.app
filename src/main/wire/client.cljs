(ns wire.client
  (:require [core]
            ["socket.io-client" :as socket-io]
            [wire.core]
            [cljs.core.async :refer [chan put! go <!]]))

;; 
;; 
;; 
;; 
;; 
;; 

(core/register-module! ::wire)

;; 
;; 
;; 
;; 
;; 
;; 

(defmethod core/initial-state ::wire []
  {::status :connecting})

;; 
;; 
;; 
;; 
;; 

(defn send-to-server [input & msgs]
  (let [effect {:type ::send-to-server 
                :msgs msgs}]
    (core/append-effect input effect)))

(def to-server-msgs-chan (chan))

(defmethod core/handle-effect! ::send-to-server [input]
  (let [msgs (-> input :effect :msgs)]
    (put! to-server-msgs-chan msgs)
    input))
  
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

(defn get-session-id! []
  (js->clj (.getItem js/localStorage "session-id")))

(defn set-session-id! [session-id]
  (when (string? session-id)
    (.setItem js/localStorage "session-id" session-id)))

(defn send-session-id! [^js socket]
  (let [session-id (get-session-id!)]
    (println "send-session-id!" session-id)
    (.emit socket "session-id" session-id)))

(defn recieved-session-id! [^js socket session-id]
  (println "recieved-session-id!" session-id)
  (set-session-id! session-id)
  (send-session-id! socket))

(defmethod core/subscriptions! ::wire [{:keys [dispatch!]}]
  (let [socket-config {:query {:session-id (get-session-id!)}}
        socket (socket-io/io server-url (clj->js socket-config))] 
    
    (.on socket "connect"
         (fn []
           (send-session-id! socket)))
    
    (.on socket "session-id"  
         (fn [session-id]
           (recieved-session-id! socket session-id)))
    
    (.on socket "to-client-msgs" 
         (fn [encoded-msgs]
           (let [msgs (wire.core/edn-decode encoded-msgs)]
             (doseq [msg msgs]
               (dispatch! msg)))))
    (go 
      (while true
        (let [msgs (<! to-server-msgs-chan)
              encoded-msgs (wire.core/edn-encode msgs)]
          (.emit socket "to-server-msgs" encoded-msgs))))))
