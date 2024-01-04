(ns client.location
  (:require [clojure.string]))



(defn remove-leading-question-mark [query-string]
  (if (clojure.string/starts-with? query-string "?") (subs query-string 1) query-string))

(defn parse-query-params [query-string]
  (let [key-val-string (remove-leading-question-mark query-string)
        key-val-strings (clojure.string/split key-val-string #"&")
        key-val-pairs (map #(clojure.string/split % #"=") key-val-strings)
        key-val-pairs-cleaned (filter #(= 2 (count %)) key-val-pairs)]
    (reduce (fn [params [key val]] (assoc params (keyword key) val))
          {}
          key-val-pairs-cleaned)))
  

(defn get-location! []
  (let [location (.-location js/window)
        pathname (.-pathname location)
        query-string (.-search location)
        query-params (parse-query-params query-string)]
    {:pathname pathname
     :query-params query-params}))

(println (pr (get-location!)))