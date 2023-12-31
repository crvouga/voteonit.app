(ns ui.textfield)

(defmulti view (fn [input] (-> input :variant)))

(defn e->value [e]
  (-> e .-target .-value))

(defn assoc-on-input-handler [input]
  (let [on-value (-> input :on-value (fnil identity))
        on-input (fn [e] (on-value (e->value e)))
        input-new (-> input 
                      (assoc :on-input on-input) 
                      (dissoc :on-value))]
    input-new))

(defmethod view :contained
  [input]
  (println "input" input)
  [:div.w-full
   [:label.text-sm.font-bold (str (:label input))]
   [:input.rounded.border.p-2.w-full.border-blue-500.border-2 (assoc-on-input-handler input)]])

(defmethod view :default [input]
  (view (assoc input :variant :contained)))