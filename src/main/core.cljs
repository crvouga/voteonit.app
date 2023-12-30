(ns core)

(defmulti step (fn [input] (-> input :msg :t)))

(defn output [input]
  {:model (-> input :model)
   :effects []})

(defn add-effect [input f]
  (update input :effects conj f))