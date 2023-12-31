(ns server
  (:require ["http" :as http]
            ["serve-static" :as serve-static]
            ["socket.io" :as socket-io]
            [wire.server]))

(defn request-handler [req res]
  (println (-> req .-url))
  (.end res "hello")) 

(defn serve-static-files [req res]
  (let [public-path "./public"
        serve-static-middleware (serve-static public-path)
        req-handler (fn [] (request-handler req res))]
    (serve-static-middleware req res req-handler)))

(defonce server-ref
  (volatile! nil))

(defn on-listen [err]
  (if err
    (println "server start failed")
    (println "http server running on port 3000")))

(def port-env (-> js/process.env .-PORT))
(def port (if port-env (js/parseInt port-env) 3000))

(defn on-disconnect [^js socket]
  (println "socket disconnected" socket))

(defn on-connection [^js socket]
  (println "socket connected" socket)
  (.on socket "disconnect" (fn [] (on-disconnect socket))))

(defn main []
  (println "starting server")
  (let [server (http/createServer serve-static-files)
        io (new socket-io/Server server 
                {:cors {:origin true}})]
    (.listen server port on-listen)
    (.on io "connection" on-connection)
    (vreset! server-ref server)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start []
  (println "start called")
  (main))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop [done]
  (println "stop called")
  (when-some [srv @server-ref]
    (.close srv
      (fn [err]
        (println "stop completed" err)
        (done)))))

(println "__filename" js/__filename)
