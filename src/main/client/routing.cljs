(ns client.routing
  (:require [cljs.core.async :refer [<! chan go put!]]
            [clojure.string :as string]
            [core]))
            

;; 
;; 
;; 
;; 
;; 
;; 

(core/register-module! ::routing)

(def default-route {:type :polls})

(defmethod core/initial-state ::routing []
  {::stack [default-route]})

(defn to-current-route [input]
  (-> input ::stack last))

(defn push-stack [input route]
  (update input ::stack conj route))

(defn pop-stack [input]
  (update input ::stack pop))

(defn replace-stack [input route]
  (-> input 
      (pop-stack)
      (push-stack route)))



;; 
;; 
;; 
;; 
;; 
;; 

(defn- current-route-change [route]
  {:type ::current-route-changed
   :route route})

(defmethod core/handle-msg ::current-route-changed [input]
  (let [route (-> input :msg :route)]
    (-> input (replace-stack route))))

;; 
;; 
;; 
;; 
;; 
;; 
;; 


(defn encode-map-to-url-string [m]
  (let [encode (fn [[k v]] (str (name k) "=" (js/encodeURIComponent v)))
        pairs (map encode m)]
    (clojure.string/join "&" pairs)))

(defn decode-url-string-to-map [query-string]
  (let [search-params (js/URLSearchParams. query-string)
        result {}]
    (.forEach search-params
      (fn [value key]
        (let [keyword-key (keyword key)]
          (if (contains? result keyword-key)
            (update result keyword-key conj (js/decodeURIComponent value))
            (assoc result keyword-key [(js/decodeURIComponent value)])))))
    result))
;; 
;; 
;; 
;; 
;; 
;; 

(defn push-route [input route] 
  (-> input
      (core/append-effect {:type ::push :route route})
      (push-stack route)))

(defn- push! [route]
  (js/history.pushState nil nil (encode-map-to-url-string route)))

(defmethod core/handle-effect! ::push [input]
  (let [route (-> input :effect :route)]
    (push! route)
    input))

;; 
;; 
;; 
;; 

(defn replace-route [input route] 
  (-> input
      (core/append-effect {:type ::replace-route :route route})
      (replace-stack route)))

(defn- replace-route! [route]
  (js/history.replaceState nil nil (encode-map-to-url-string route)))

(defmethod core/handle-effect! ::replace-route [input]
  (let [route (-> input :effect :route)]
    (replace-route! route)
    input))

;; 
;; 
;; 
;; 
;; 

(defn pop-route [input]
  (-> input
      (core/append-effect {:type ::pop-route})
      (pop-stack)))

(defmethod core/handle-effect! ::pop-route [input]
  (js/history.back)
  input)
  

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti view-route (fn [input] (-> input to-current-route :type)))

(defmethod view-route :default [_]
  [:div "Page not found"])

;; 
;; 
;; 
;; 
;; 
;; 

(defn- get-route! []
  (let [query-string (-> js/location .-search (string/replace-first #"^\?" ""))
        route (decode-url-string-to-map query-string)
        route-final (if (empty? route) default-route route)]
    route-final))

(def route-chan (chan))

(defn put-route! []
  (put! route-chan (get-route!)))

(defn dispatch-route-changes! [dispatch!]
  (go
    (while true
      (let [route (<! route-chan)
            msg (current-route-change route)]
        (dispatch! msg)))))

(defmethod core/subscriptions! ::routing [{:keys [dispatch!]}]
  (.addEventListener js/window "hashchange" put-route!)
  (dispatch-route-changes! dispatch!)
  (let [current-route (get-route!)]
    (put! route-chan current-route)))