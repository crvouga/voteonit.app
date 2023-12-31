(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            ["socket.io-client" :as socket-io]
            [ui.button]
            [auth.client]
            [wire.client]
            [core]))


(defn initial-state [] 
  (merge
   (auth.client/init)))

(defn view [{:keys [] :as input}] 
   [:div.w-screen.flex.flex-col.items-center.justify-center.bg-neutral-900.text-white.overflow-hidden
    {:style {:height "100dvh"}}
    [:div.flex.flex-col.gap-4.w-full.max-w-md
     [auth.client/view-login-page input]]])
  


;; 
;; 
;; 
;; 
;; 
;; 

(def output (r/atom {:state (initial-state)}))

(defn dispatch! [msg]
  (println msg)
  (let [state (-> @output :state)
        input {:state state :msg msg}
        new-output (core/handle-msg input)]
    (reset! output new-output)))

(add-watch output :run-effects (core/watch-handle-eff! dispatch!))

(def is-localhost (= "localhost" (.-hostname js/window.location)))

(print "is-localhost" is-localhost)

(def server-url (if  is-localhost "http://localhost:3000" ""))

(defn root-view [] 
  [view {:state (@output :state) 
         :dispatch! dispatch!}])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (println "client staring" (pr-str {:server-url server-url :is-localhost is-localhost}))
  (let [socket (socket-io/io server-url)]
    (println socket)
    (.emit socket "client-connected")
    (rd/render [root-view] (js/document.getElementById "root"))))