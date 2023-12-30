(ns core)

(defmulti step (fn [input] (-> input :msg :type)))

(defn ->output [input]
  {:model (-> input :model)
   :effects []})