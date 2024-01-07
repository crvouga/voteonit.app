(ns client.app
  (:require [core]
            [ui.icon]
            [client.routing]))

;; 
;; 
;; 
;; 
;; 
;; 


(defn- bottom-bar-item [{:keys [label icon on-click active?]}]
  [:button.flex.flex-col.items-center.justify-center.flex-1.h-full.gap-1.py-3
   {:on-click on-click
    :class (if active? "text-blue-500" "text-neutral-500")}
   icon
   [:p.text-xs.font-bold.uppercase label]])

(defn bottom-bar [{:keys [on-polls on-account active]}]
  [:div.w-full.flex.items-center.border-t.border-neutral-700
   
   [bottom-bar-item 
    {:label "Polls" 
     :active? (= active :polls)
     :on-click on-polls 
     :icon [ui.icon/check-to-slot {:class "w-6 h-6"}]}]
   
   [bottom-bar-item 
    {:label "Account" 
     :active? (= active :account)
     :on-click on-account 
     :icon [ui.icon/user {:class "w-6 h-6"}]}]])


