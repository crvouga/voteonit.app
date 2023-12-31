(ns server
  (:require ["http" :as http]
            ["serve-static" :as serve-static]
            ["socket.io" :as socket-io]
            ["cors" :as cors]
            ["express" :as express]
            [wire.server]))

(def app (express))

(def cors-config {:origin "*"
                  :methods ["GET" "POST" "DELETE" "OPTIONS" "PUT" "PATCH"]
                  :optionsSuccessStatus 200
                  :credentials false})

(.use app (cors (clj->js cors-config)))

(def public-path "./public")
(def serve-static-middleware (serve-static public-path))
(.use app serve-static-middleware)

(defn request-handler [_req res]
  (println "request-handler")
  (.end res "hello from server"))

(.get app "*" request-handler)

(def http-server (http/createServer app))

(def socket-config {:cors {:origin "*"
                           :methods ["GET" "POST" "DELETE" "OPTIONS" "PUT" "PATCH"]
                           :credentials false}})

(def io (new socket-io/Server http-server (clj->js socket-config)))

(defn on-connection [^js socket]
  (println "socket connected" socket)
  (.on socket "disconnect" #(println "socket disconnected" socket)))

(.on io "connection" on-connection)

(defonce server-ref (volatile! nil))

(def port-env (-> js/process.env .-PORT))
(def port (if port-env (js/parseInt port-env) 3000))

(defn on-listen [err]
  (if err
    (println "server start failed" err)
    (println "http server running on port" port)))

(defn main []
  (println "[main] server starting")
  (.listen http-server port on-listen)
  #_(vreset! server-ref http-server))

(defn start []
  (main))

(defn stop [done]
  (println "server stopping")
  (let [on-close (fn [err]
                   (println "server closed" err)
                   (done))]
  (when-some [server @server-ref]
    (.close server on-close))))
