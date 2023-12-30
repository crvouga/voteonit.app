(ns ui.button)

(defmulti view (fn [input] (-> input :variant)))

(defmethod view :contained
  [input]
  [:button.active:opacity-60.hover:opacity-90.p-3.text-lg.font-bold.bg-blue-500.rounded.text-white.ring-offset-1 input (:text input)])

(defmethod view :default [input] (view (assoc input :variant :contained)))