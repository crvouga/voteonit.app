(ns app.auth.client
  (:require [ui.textfield]
            [core :refer [step]]))

(defn init []
  {::email ""})

(defmethod step ::user-inputted-email [input]
  (assoc-in input  [:model ::email] (-> input :msg :email)))

(defn view-login-page [{:keys [model dispatch!]}] 
  [:div 
   [ui.textfield/view 
    {:value (::email model) 
     :on-value #(dispatch! {:type ::user-inputted-email :email %})}]])