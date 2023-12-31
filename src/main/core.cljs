(ns core)

(defmulti step (fn [input] (-> input :msg :type)))

(defn add-effect [input f]
  (update input :effects conj f))

(defn run-effects! [dispatch!]
  (fn [_ _ _ new-state]
    (let [new-effects (-> new-state :effects)]
      (doseq [eff new-effects]
        (eff dispatch!)))))