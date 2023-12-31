(ns wire.core
  (:require [cljs.reader :as reader]))

(defn edn-encode [edn]
  (pr-str edn))

(defn edn-decode [encoded-edn]
  (reader/read-string encoded-edn))