(ns server
  (:require ["http" :as http]
            ["serve-static" :as serve-static]
            ["cors" :as cors]
            [core]
            ["express" :as express]
            [wire.server]
            [auth.server]))


;; 
;; 
;; 
;; State
;; 
;; 
;; 


(defn initial-state []
  (merge
   (auth.server/initial-state)))



;; 
;; 
;; 
;; Server
;; 
;; 
;; 

(def app (express))


(def cors-config {:origin "*"
                  :methods ["GET" "POST" "DELETE" "OPTIONS" "PUT" "PATCH"]
                  :optionsSuccessStatus 200
                  :credentials false})
(.use app (cors (clj->js cors-config)))

;; 
;; Static files
;; 

(def public-path "./public")
(def serve-static-middleware (serve-static public-path))
(.use app serve-static-middleware)

(defn request-handler [_req res]
  (println "request-handler")
  (.end res "hello from server"))
(.get app "*" request-handler)

;; 
;; Http Server
;; 

(def http-server (http/createServer app))

(defonce server-ref (volatile! nil))

(def port-env (-> js/process.env .-PORT))
(def port (if port-env (js/parseInt port-env) 3000))

(defn on-listen [err]
  (if err
    (println "server start failed" err)
    (println "http server running on port" port)))

;; 
;; 
;; Main
;; 
;; 

(def state! (atom (initial-state)))

(defn dispatch! [msg]
  (let [stepped (core/step! (merge @state! {:msg msg}))]
    (reset! state! stepped)))
  
(defn main []
  (println "[main] server starting")
  (.listen http-server port on-listen)
  (wire.server/subscriptions! http-server dispatch!)
  (vreset! server-ref http-server))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start []
  (main))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop [done]
  (println "server stopping")
  (let [on-close (fn [err]
                   (println "server closed" err)
                   (done))]
  (when-some [server @server-ref]
    (.close server on-close))))
