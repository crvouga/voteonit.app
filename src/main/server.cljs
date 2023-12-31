(ns server
  (:require ["http" :as http]
            ["serve-static" :as serve-static]))

(defn request-handler [_req res]
  (.end res "hello"))

(defn serve-static-files [req res]
  (let [public-path "./public" ; Adjust the path to your public directory
        serve-static-middleware (serve-static public-path)
        req-handler (fn [] (request-handler req res))]
    (serve-static-middleware req res req-handler)))

(defonce server-ref
  (volatile! nil))

(defn main []
  (println "starting server")
  (let [server (http/createServer serve-static-files)
        port-env (-> js/process.env .-PORT)
        port (if port-env (js/parseInt port-env) 3000)
        on-listen-cb (fn [err]
                       (if err
                         (println "server start failed")
                         (println "http server running on port 3000")))]
    (.listen server port on-listen-cb)
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
