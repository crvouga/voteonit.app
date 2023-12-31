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

(defmethod handle-msg :default  [input]
  (println "Unhandled msg" input)
  input)

(defmethod handle-cmd :default [input]
  (println "Unhandled cmd" input)
  input)

(defmethod handle-eff! :default [input]
  (println "Unhandled eff!" input)
  input)

;; 
;; 
;; 
;; 

(defn append-effect [input eff]
  (update input :effects conj eff))

(defn append-command [input cmd]
  (update input :commands conj cmd))

;; 
;; 
;; 
;; 
;; 

(defn ->effects [output]
  (or (-> output :effects seq) []))

(defn ->commands [output]
  (or (-> output :commands seq) []))

(defn ->state [output] 
  (-> output :state))

(defn step! [input]
  (let [output-from-msg (handle-msg input)]
    (loop [running-output output-from-msg]
     (let [effects (->effects running-output)
           commands (->commands running-output)
           state (or (->state running-output) (->state output-from-msg))]
       (cond 
         (first commands)
         (let [input {:state state :cmd (first commands)}
               output-from-cmd (handle-cmd input)
               output-next {:state (or (->state output-from-cmd) state)
                            :effects (concat effects (->effects output-from-cmd))
                            :commands (concat (rest commands) (->commands output-from-cmd))}]
           (recur output-next))
         
         (first effects)
         (let [input {:state state :eff (first effects)}
               output-from-eff (handle-eff! input)
               output-next {:state (or (->state output-from-eff) state)
                            :effects (concat (rest effects) (->effects output-from-eff))
                            :commands (concat commands (->commands output-from-eff))}]
           (recur output-next))
         
         :else
         running-output))))

)
  
;; 
;; 
;; 
;; 
;; 
;; 
;; 


(defn publish-event [input event]
  (append-effect input {:type ::publish :event event}))

(def event-subscribers (atom #{}))

(defn register-event-handler! [handle-event]
  (swap! event-subscribers conj handle-event))

(defn publish! [event]
  (doseq [handle-event @event-subscribers]
    (handle-event {:event event :state {}})))

(defmethod handle-eff! ::publish [input]
    (publish! (-> input :eff :event)))