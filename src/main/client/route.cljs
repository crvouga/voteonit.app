(ns client.route
  (:require [clojure.string]
            [clojure.edn]
            [clojure.spec.alpha :as spec]
            [cljs.core.async :as async]))

;; 
;; 
;; 
;; 
;; 
;; 

(spec/def ::type keyword?)

(spec/def ::route (spec/keys :req [::type]))

(def default-route {:type nil})

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defn- dispatch-pop-state! []
  (.dispatchEvent js/window (js/Event. "popstate")))

(defn- push-url! [url]
  (.pushState (.-history js/window) nil "" url)
  (dispatch-pop-state!))

(defn- replace-url! [url]
  (.replaceState (.-history js/window) nil "" url)
  (dispatch-pop-state!))

(defn- back! []
  (.back (.-history js/window))
  (dispatch-pop-state!))

(defn get-url! []
  js/window.location.href)

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defn- edn->base-64 [edn]
  (js/btoa (pr-str edn)))

(defn- base-64->edn [base-64]
  (try
    (when base-64 
      (clojure.edn/read-string (js/atob base-64)))
    (catch :default _e
      nil)))


;; 
;; 
;; 
;; 
;; 
;; 
;; 

(def route-key "route")

(defn- route->url [route]
  (let [encoded-route (edn->base-64 route)
        url! (js/URL. (.-href js/window.location))]
    (.set (.-searchParams url!) route-key encoded-route)
    (.toString url!)))

(defn- url->route [url]
  (let [url! (js/URL. url)
        encoded-route (.get (.-searchParams url!) route-key)
        decoded-route (base-64->edn encoded-route)]
    (when (spec/valid? ::route decoded-route)
      decoded-route)))

;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; 


(defn push-route! [route]
  (let [url (route->url route)]
    (push-url! url)))

(defn replace-route! [route]
  (let [url (route->url route)]
    (replace-url! url)))

(defn pop-route! []
  (back!))


;; 
;; 
;; 
;; 
;; 
;; 
;; 

(def route-chan! (async/chan))

(defn- get-route! []
  (let [url (get-url!)
        route (url->route url)]
    route))

(defn- put-route! []
  (let [got-route (get-route!)
        new-route (or got-route default-route)]
    (async/put! route-chan! new-route)))

(defn start-listening! []
  (replace-route! (get-route!))
  (put-route!)
  (.addEventListener js/window "popstate" put-route!)
  (.addEventListener js/window "pushstate" put-route!)
  (fn []
    (.removeEventListener js/window "popstate" put-route!)
    (.removeEventListener js/window "pushstate" put-route!)))

