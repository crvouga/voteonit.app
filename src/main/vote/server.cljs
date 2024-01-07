(ns vote.server
  (:require [core]))


(core/register-module! ::vote)

(defmethod core/on-init ::vote []
  {::polls-by-id {}})


(defmethod core/msgs! ::vote [])

(defmethod core/on-evt ::vote [input] input)