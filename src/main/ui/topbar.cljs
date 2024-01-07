(ns ui.topbar
  (:require [core]))

(defn view [{:keys [title]}]
  [:div.w-full.h-16.text-white.text-2xl.font-bold.flex.items-center.justify-center.px-4.border-b.border-neutral-700
   [:p.text-center.text-xl.font-bold.flex-1 title]])
