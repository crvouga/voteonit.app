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

(def msg ::msg)

(def eff ::eff)

(def cmd ::cmd)

(def evt ::evt)

(defmulti on-init (fn [input] (-> input ::module)))

(defmulti on-msg (fn [input] (-> input ::msg :type)))

(defmulti on-eff! (fn [input] (-> input ::eff :type)))

(defmulti msgs! (fn [input] (-> input ::module)))

(defmulti on-cmd (fn [input] (-> input ::cmd :type)))

(defmulti on-evt (fn [input] [(-> input ::module) (-> input ::evt :type)]))




;; 
;; 
;; 
;; 
;; 
;; 


(defmethod on-init :default []
  (println "@modules" @modules)
  (reduce 
   (fn [acc module] (merge acc (on-init (assoc acc ::module module))))
   {}
   @modules))

(defmethod on-cmd :default [input]
  (println "Unhandled cmd" input)
  input)

(defmethod on-msg :default  [input]
  (println "Unhandled msg" input)
  input)

(defmethod on-evt :default [input]
  (println "Unhandled event" input)
  input)

(defmethod msgs! nil [input] 
  (doseq [module @modules]
    (msgs! (assoc input ::module module))))

(defmethod msgs! :default [input] 
  (println "Unhandled msgs!" input))


(defmethod on-eff! :default [input]
  (println "Unhandled eff!" input)
  input)


;; 
;; 
;; 
;; 

(defn add-eff [input effect]
  (update input ::effs conj effect))

(defn add-cmd [input command]
  (update input ::cmds conj command))

;; 
;; 
;; 
;; 
;; 

(defn- ->effs [output]
  (or (-> output ::effs seq) []))

(defn- ->cmds [output]
  (or (-> output ::cmds seq) []))

(defn print-msg [input]
  (let [command (-> input ::cmd)
        msg (-> input ::msg)
        effect (-> input ::eff)]
    
    (when command 
      (println (str "[cmd] " (pr-str command) "\n")))
    
    (when msg 
      (println (str "[msg] " (pr-str msg) "\n")))
    
    (when effect 
      (println (str "[eff] " (pr-str effect) "\n")))))

(defn ->on-cmd-input [input cmd]
  (-> input (dissoc ::cmds ::effs) (assoc ::cmd cmd)))

(defn ->on-msg-input [input msg]
  (-> input (dissoc ::cmds ::effs) (assoc ::msg msg)))

(defn ->on-eff-input [input eff]
  (-> input (dissoc ::cmds ::effs) (assoc ::eff eff)))


(declare stepper-recur!)

(defn stepper-cmds! [output cmds]
  (let [input (->on-cmd-input output (first cmds))
        output-from-command (on-cmd input)
        output-next (merge output-from-command
                           {::effs (concat (->effs output) (->effs output-from-command))
                            ::cmds (concat (rest cmds) (->cmds output-from-command))})]
    (print-msg input)
    (stepper-recur! output-next)))

(defn stepper-effs! [output effs]
  (let [input (->on-eff-input output (first effs))
        output-from-effect (on-eff! input)
        output-next (merge output-from-effect
                           {::effs (concat (rest effs) (->effs output-from-effect))
                            ::cmds (concat (->cmds output) (->cmds output-from-effect))})]
    (print-msg input)
    (stepper-recur! output-next)))

(defn stepper-recur! [output]
  (let [effs (->effs output)
        cmds (->cmds output)]
    (cond 
      (first cmds) 
      (stepper-cmds! output cmds)
      
      (first effs)
      (stepper-effs! output effs)
      
      :else
      output)))

(defn stepper! 
  [state msg] 
  (let [input (->on-msg-input state msg)]
    (print-msg input)
    (stepper-recur! (on-msg input))))
    
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
  (add-eff input {:type ::publish-event ::evt event}))

(defmethod on-eff! ::publish-event [input] 
  (loop [running-output (dissoc input ::eff)
         modules @modules]
    (if (empty? modules)
        running-output
        (let [event (-> input ::eff ::evt)
              output-from-event (on-evt (assoc input ::evt event ::module (first modules)))
              output-next (merge output-from-event
                                 {::effs (concat (->effs running-output) (->effs output-from-event))
                                  ::cmds (concat (->cmds running-output) (->cmds output-from-event))})] 
          (println "output-next" output-next)
          (recur output-next (rest modules))))))