(ns ui.button)

(defmulti view (fn [input] (-> input :variant)))

(defmethod view :contained
  [input]
  [:button.p-3.text-lg.font-bold.bg-blue-500.rounded.text-white input (:text input)])

(defmethod view :default [input] (view (assoc input :variant :contained)))