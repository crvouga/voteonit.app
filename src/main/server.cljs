(ns server
  (:require ["http" :as http]
            ["serve-static" :as serve-static]
            [core]
            ["express" :as express]
            [wire.server]
            [vote.server]
            [auth.server]))

;; 
;; 
;; 
;; Server
;; 
;; 
;; 

(def app! (express))

;; 
;; 
;; Static files
;; 
;; 

(def public-path "./public")
(def serve-static-middleware (serve-static public-path))
(.use app! serve-static-middleware)

(defn request-handler [_req res]
  (println "request-handler")
  (.end res "hello from server"))
(.get app! "*" request-handler)

;; 
;; 
;; Http Server
;; 
;; 

(def port-env (-> js/process.env .-PORT))
(def port (if port-env (js/parseInt port-env) 3000))

(defn on-listen [err]
  (if err
    (println "server start failed" err)
    (println "[http-server] Running on port" port)))

(defn listen! [^js http-server!]
  (.listen http-server! port on-listen))

;; 
;; 
;; Main
;; 
;; 

(def state! (atom (core/on-init {})))

(defn dispatch! [msg]
  (core/step! state! msg))

(def http-server! (http/createServer app!))

(defonce server-ref (volatile! nil))

(defn main []
  (println "[main] server starting")
  (listen! http-server!)
  (core/msgs! {:http-server! http-server! 
               :dispatch! dispatch!})
  (vreset! server-ref http-server!))

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
