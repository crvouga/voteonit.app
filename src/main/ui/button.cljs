(ns ui.button)

(defmulti view (fn [input] (-> input :variant)))

(defmethod view :contained
  [input]
  [:button.w-full.active:opacity-60.hover:opacity-90.p-2.text-base.font-bold.bg-blue-500.rounded.text-white.ring-offset-1 input (:text input)])

(defmethod view :default [input] (view (assoc input :variant :contained)))