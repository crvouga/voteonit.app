(ns core)

;; 
;; 
;; 
;; 
;; 
;; 

(def modules (atom #{}))

(defn register-module! [module]
  (swap! modules conj module))

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti initial-state (fn [input] (-> input :module)))

(defmulti handle-msg (fn [input] (-> input :msg :type)))

(defmulti handle-command (fn [input] (-> input :command :type)))

(defmulti handle-event (fn [input] [(-> input :module) (-> input :msg :type)]))

(defmulti handle-effect! (fn [input] (-> input :effect :type)))

(defmulti subscriptions! (fn [input] (-> input :module)))

;; 
;; 
;; 
;; 
;; 
;; 

(defmethod initial-state :default []
  (println "@modules" @modules)
  (reduce 
   (fn [acc module] (merge acc (initial-state (assoc acc :module module))))
   {}
   @modules))

(defmethod subscriptions! nil [input] 
  (doseq [module @modules]
    (subscriptions! (assoc input :module module))))

(defmethod subscriptions! :default [input] 
  (println "Unhandled sub" input))

(defmethod handle-msg :default  [input]
  (println "Unhandled msg" input)
  input)

(defmethod handle-command :default [input]
  (println "Unhandled cmd" input)
  input)

(defmethod handle-effect! :default [input]
  (println "Unhandled eff!" input)
  input)


;; 
;; 
;; 
;; 

(defn append-effect [input effect]
  (update input :effects conj effect))

(defn append-command [input command]
  (update input :commands conj command))

;; 
;; 
;; 
;; 
;; 

(defn ->effects [output]
  (or (-> output :effects seq) []))

(defn ->commands [output]
  (or (-> output :commands seq) []))

(defn print-msg [input]
  (let [command (-> input :command)
        msg (-> input :msg)
        effect (-> input :effect)]
    (when command (println (str "[command] " (pr-str command) "\n")))
    (when msg (println (str "[msg] " (pr-str msg) "\n")))
    (when effect (println (str "[effect] " (pr-str effect) "\n")))))

(defn stepper! 
  [state msg]
  (let [input {:state state :msg msg}
        output-from-msg (handle-msg input)]
    (print-msg input)
    (loop [running-output output-from-msg]
     (let [effects (->effects running-output)
           commands (->commands running-output)]
       (cond 
         (first commands) 
         (let [input (-> running-output (dissoc :commands :effects) (assoc :command (first commands)))
               output-from-command (handle-command input)
               output-next (merge output-from-command
                                  {:effects (concat effects (->effects output-from-command))
                                   :commands (concat (rest commands) (->commands output-from-command))})]
           (print-msg input)
           (recur output-next))
         
         (first effects)
         (let [input (-> running-output (dissoc :commands :effects) (assoc :effect (first effects)))
               output-from-effect (handle-effect! input)
               output-next (merge output-from-effect
                                  {:effects (concat (rest effects) (->effects output-from-effect))
                                   :commands (concat commands (->commands output-from-effect))})]
           (print-msg input)
           (recur output-next))
         
         :else
         running-output)))))

(defn step! 
  [state! msg] 
  (reset! state! (stepper! @state! msg)))
  
;; 
;; 
;; 
;; 
;; 
;; 
;; 


(defn publish-event [input event]
  (append-effect input {:type ::publish-event :msg event}))

(defmethod handle-effect! ::publish-event [input] 
  (loop [running-output (dissoc input :effect)
         modules @modules]
    (if (empty? modules)
        running-output
        (let [msg  (-> input :effect :msg)
              output-from-event (handle-event (assoc input :msg msg :module (first modules)))
              output-next (merge output-from-event
                                 {:effects (concat (->effects running-output) (->effects output-from-event))
                                  :commands (concat (->commands running-output) (->commands output-from-event))})] 
          (println "output-next" output-next)
          (recur output-next (rest modules))))))