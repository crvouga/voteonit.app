(ns core)

(defmulti step (fn [input] (-> input :msg :type)))

(defn add-effect [input f]
  (update input :effects conj f))