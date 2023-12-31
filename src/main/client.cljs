(ns client
  (:require [reagent.dom :as rd] 
            [reagent.core :as r]
            [ui.button]
            [auth.client]
            [wire.client]
            [cljs.core.async :refer [chan put! go <!]]
            [core]))

;; 
;; 
;; 
;; State
;; 
;; 
;; 

(defn initial-state [] 
  (merge
   (auth.client/initial-state)))

;; 
;; 
;; 
;; View
;; 
;; 
;; 

(defn view [{:keys [] :as input}] 
   [:div.w-screen.flex.flex-col.items-center.justify-center.bg-neutral-900.text-white.overflow-hidden
    {:style {:height "100dvh"}}
    [:div.flex.flex-col.gap-4.w-full.max-w-md
     [:pre.text-sm (pr-str (:state input))]
     [auth.client/view-login-page input]]])
  


;; 
;; 
;; 
;; 
;; 
;; 

(def state (r/atom (initial-state)))

(defn dispatch! [msg]
  (println msg)
  (let [stepped (core/step! {:state @state :msg msg})]
    (reset! state (-> stepped :state))))

(go
  (while true
    (let [msgs (<! wire.client/to-client-msgs-chan)]
      (doseq [msg msgs]
        (dispatch! msg)))))

(defn root-view [] 
  [view {:state @state
         :dispatch! dispatch!}])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (wire.client/attach-web-socket!)
  (rd/render [root-view] (js/document.getElementById "root")))