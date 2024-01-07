(ns vote.client.routes
  (:require [client.routing]))

(defn polls [] 
  {client.routing/route-name ::polls})