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

(defmulti on-init (fn [input] (-> input :module)))

(defmulti on-msg (fn [input] (-> input :msg :type)))

(defmulti on-eff! (fn [input] (-> input :eff :type)))

(defmulti msgs! (fn [input] (-> input :module)))

(defmulti on-cmd (fn [input] (-> input :cmd :type)))

(defmulti on-evt (fn [input] [(-> input :module) (-> input :msg :type)]))

;; 
;; 
;; 
;; 
;; 
;; 


(defmethod on-init :default []
  (println "@modules" @modules)
  (reduce 
   (fn [acc module] (merge acc (on-init (assoc acc :module module))))
   {}
   @modules))

(defmethod on-cmd :default [input]
  (println "Unhandled cmd" input)
  input)

(defmethod on-msg :default  [input]
  (println "Unhandled msg" input)
  input)

(defmethod on-evt :default [input]
  (println "Unhandled event" (-> input :msg :type))
  input)

(defmethod msgs! nil [input] 
  (doseq [module @modules]
    (msgs! (assoc input :module module))))

(defmethod msgs! :default [input] 
  (println "Unhandled sub" input))


(defmethod on-eff! :default [input]
  (println "Unhandled eff!" input)
  input)


;; 
;; 
;; 
;; 

(defn append-effect [input effect]
  (update input :effs conj effect))

(defn append-command [input command]
  (update input :cmds conj command))

;; 
;; 
;; 
;; 
;; 

(defn ->effects [output]
  (or (-> output :effs seq) []))

(defn ->commands [output]
  (or (-> output :cmds seq) []))

(defn print-msg [input]
  (let [command (-> input :cmd)
        msg (-> input :msg)
        effect (-> input :eff)]
    (when command (println (str "[command] " (pr-str command) "\n")))
    (when msg (println (str "[msg] " (pr-str msg) "\n")))
    (when effect (println (str "[effect] " (pr-str effect) "\n")))))

(defn stepper! 
  [state msg]
  (let [input {:state state :msg msg}
        output-from-msg (on-msg input)]
    (print-msg input)
    (loop [running-output output-from-msg]
     (let [effects (->effects running-output)
           commands (->commands running-output)]
       (cond 
         (first commands) 
         (let [input (-> running-output (dissoc :cmds :effs) (assoc :cmd (first commands)))
               output-from-command (on-cmd input)
               output-next (merge output-from-command
                                  {:effs (concat effects (->effects output-from-command))
                                   :cmds (concat (rest commands) (->commands output-from-command))})]
           (print-msg input)
           (recur output-next))
         
         (first effects)
         (let [input (-> running-output (dissoc :cmds :effs) (assoc :eff (first effects)))
               output-from-effect (on-eff! input)
               output-next (merge output-from-effect
                                  {:effs (concat (rest effects) (->effects output-from-effect))
                                   :cmds (concat commands (->commands output-from-effect))})]
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

(defmethod on-eff! ::publish-event [input] 
  (loop [running-output (dissoc input :eff)
         modules @modules]
    (if (empty? modules)
        running-output
        (let [msg  (-> input :eff :msg)
              output-from-event (on-evt (assoc input :msg msg :module (first modules)))
              output-next (merge output-from-event
                                 {:effs (concat (->effects running-output) (->effects output-from-event))
                                  :cmds (concat (->commands running-output) (->commands output-from-event))})] 
          (println "output-next" output-next)
          (recur output-next (rest modules))))))