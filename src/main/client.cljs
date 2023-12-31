(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
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

(wire.client/start-web-socket!)

(defn root-view []
  (let [state (-> @output :state)
        input {:state state :dispatch! dispatch!}]
    [view input]))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (rd/render [root-view] (js/document.getElementById "root")))

