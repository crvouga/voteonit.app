(ns auth.client.routes
  (:require [client.routing]))

(defn account []
  {client.routing/route-name ::account})

(defn login []
  {client.routing/route-name ::login})