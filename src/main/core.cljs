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

(defn add-eff [input eff]
  (update input ::effs conj eff))

(defn add-cmd [input cmd]
  (update input ::cmds conj cmd))

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
  (let [cmd (-> input ::cmd)
        msg (-> input ::msg)
        eff (-> input ::eff)
        evt (-> input ::evt)]
    
    (when evt
      (println (str "[evt] " (pr-str evt) "\n")))
    
    (when cmd 
      (println (str "[cmd] " (pr-str cmd) "\n")))
    
    (when msg 
      (println (str "[msg] " (pr-str msg) "\n")))
    
    (when eff 
      (println (str "[eff] " (pr-str eff) "\n")))))

(defn dissoc-outputs [input]
  (dissoc input ::cmds ::effs))

(defn ->on-cmd-input [input cmd]
  (-> input dissoc-outputs (assoc ::cmd cmd)))

(defn ->on-msg-input [input msg]
  (-> input dissoc-outputs (assoc ::msg msg)))

(defn ->on-eff-input [input eff]
  (-> input dissoc-outputs (assoc ::eff eff)))

(defn- stepper-cmds! [output cmds]
  (let [input (->on-cmd-input output (first cmds))
        output-from-cmd (on-cmd input)
        next-effs (concat (->effs output) (->effs output-from-cmd))
        next-cmds (concat (rest cmds) (->cmds output-from-cmd))
        next-output (merge output-from-cmd {::effs next-effs ::cmds next-cmds})]
    (print-msg input)
    next-output))

(defn- stepper-effs! [output effs]
  (let [input (->on-eff-input output (first effs))
        output-from-eff (on-eff! input)
        next-effs (concat (rest effs) (->effs output-from-eff))
        next-cmds (concat (->cmds output) (->cmds output-from-eff))
        next-output (merge output-from-eff {::effs next-effs ::cmds next-cmds})]
    (print-msg input)
    next-output))

(defn- stepper-recur! [output]
  (let [effs (->effs output) 
        cmds (->cmds output)]
    (cond 
      (first cmds) 
      (stepper-recur! (stepper-cmds! output cmds))
      
      (first effs)
      (stepper-recur! (stepper-effs! output effs))
      
      :else
      output)))

(defn- stepper! 
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
              next-effs (concat (->effs running-output) (->effs output-from-event))
              next-cmds (concat (->cmds running-output) (->cmds output-from-event))
              next-output (merge output-from-event {::effs next-effs ::cmds next-cmds})] 
          (print-msg event)
          (recur next-output (rest modules))))))