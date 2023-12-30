(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]))

(def state (r/atom {:count 0}))

(defn- increment! []
  (swap! state update :count inc))

(defn- hello-world []
  [:ul
   [:li "Hello"]
   [:button {:on-click increment!} (str "Count: " (-> @state :count))]
   [:li {:style {:color "red"}} "World!"]])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init []
  (println "Hello World")
  (rd/render [hello-world] (js/document.getElementById "root")))

