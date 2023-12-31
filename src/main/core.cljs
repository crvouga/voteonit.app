(ns core)

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti handle-msg (fn [input] (-> input :msg :type)))

(defmulti handle-cmd (fn [input] (-> input :cmd :type)))

(defmulti handle-eff! (fn [input] (-> input :eff :type)))

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defn add-eff [input f]
  (update input :eff conj f))

(defn add-cmd [input f]
  (update input :cmd conj f))

(defn watch-handle-eff! [dispatch!]
  (fn [_ _ _ new-state]
    (let [new-effects (-> new-state :eff)
          model (-> new-state :state)]
      (doseq [effect new-effects]
        (handle-eff! {:dispatch! dispatch! :effect effect :state model})))))


;; 
;; 
;; 
;; 
;; 
;; 
;; 


(def event-subscribers (atom #{}))

(defn register-event-handler! [handle-event]
  (swap! event-subscribers conj handle-event))

(defn publish! [event]
  (doseq [handle-event @event-subscribers]
    (handle-event {:event event :state {}})))

(defn publish [input event]
  (add-eff input {:type ::publish :event event}))

(defmethod handle-eff! ::publish [input]
    (publish! (-> input :effect :event)))