(ns vote.create.server
  (:require [core]
            [wire.server]
            [vote.create.core]))

(defmethod core/on-msg vote.create.core/create-poll [input]
  input)