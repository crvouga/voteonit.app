(ns vote.server
  (:require [core]))


(core/register-module! ::vote)

(defmethod core/initial-state ::vote []
  {::polls-by-id {}})