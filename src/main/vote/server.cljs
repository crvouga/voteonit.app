(ns vote.server
  (:require [core]))


(defmethod core/initial-state []
  {::polls-by-id {}})