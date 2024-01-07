(ns auth.client.routes
  (:require [client.routing]))

(def path-account ::path-account)

(defn route-account [] 
  {client.routing/path path-account})

(def path-login ::path-login)

(defn route-login [] 
  {client.routing/path path-login})