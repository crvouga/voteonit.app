(ns app.auth.client
  (:require [ui.textfield]
            [ui.button]
            [core :refer [step]]))

;; 
;; 
;; 
;; 
;; 
;; 

(defn init []
  {::email ""})

(defmethod step ::user-inputted-email [input]
  (assoc-in input  [:model ::email] (-> input :msg :email)))


(defmethod step ::clicked-send-login-link [input]
  (let [email (-> input :model ::email)]
    (println "clicked-send-login-link" email)
    input))

;; 
;; 
;; 
;; 
;; 

(defn view-login-page [{:keys [model dispatch!]}] 
  [:div.flex.flex-col.gap-4.items-center.justify-center.w-full.p-6
   [:h1.text-5xl.font-bold.w-full.text-left.text-blue-500 "voteonit.app"]
   [ui.textfield/view 
    {:label "Email"
     :value (::email model) 
     :on-value #(dispatch! {:type ::user-inputted-email :email %})}]
   [ui.button/view {:text "Send login link" :on-click #(dispatch! {:type ::clicked-send-login-link})}]
   [:p.text-neutral-800.text-lg "Or"]
   [ui.button/view {:text "Continue as guest"}]])