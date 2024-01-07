(ns vote.client.routes
  (:require [client.routing]))

(def path-polls ::path-polls)

(defn route-polls [] 
  {client.routing/path path-polls})