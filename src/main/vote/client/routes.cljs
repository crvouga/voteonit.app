(ns vote.client.routes
  (:require [client.routing]))

(defn polls [] 
  {client.routing/route-name ::polls})

(defn poll-details [poll-id] 
  {client.routing/route-name ::poll-details :poll-id poll-id})

(defn create-poll []
  {client.routing/route-name ::create-poll})