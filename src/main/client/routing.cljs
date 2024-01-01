(ns client.routing)


(defn initial-state []
  {::route :login})

(defn decode-route []
  (let [path (.-pathname js/location)
        route (subs path 1)]
    route))


(defmulti view-screen (fn [input] (-> input :state ::route)))
