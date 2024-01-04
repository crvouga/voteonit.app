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

(def module ::module)

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti on-init module)

(defmulti on-msg msg)

(defmulti on-eff! eff)

(defmulti msgs! module)

(defmulti on-cmd cmd)

(defmulti on-evt (juxt module evt))


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
;; 
;; 
;; 

(defn add-eff [input effect-type & effect-payload]
  (let [effect (merge effect-payload {::eff effect-type})]
    (update input ::effs conj effect)))

(defn add-cmd [input command]
  (update input ::cmds conj command))

(defn add-evt [input event]
  (update input ::evts conj event))

;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defn- ->effs [output]
  (or (-> output ::effs seq) []))

(defn- ->cmds [output]
  (or (-> output ::cmds seq) []))

(defmulti print-msg (fn [input] (first (filter input [::cmd ::msg ::eff ::evt]))))

(defmethod print-msg cmd [input]
  (println (str "[cmd] " (pr-str (-> input cmd)) "\n")))

(defmethod print-msg msg [input]
  (println (str "[msg] " (pr-str (-> input msg)) "\n")))

(defmethod print-msg eff [input]
  (println (str "[eff] " (pr-str (-> input eff)) "\n")))

(defmethod print-msg evt [input]
  (println (str "[evt] " (pr-str (-> input evt)) "\n")))

(defmethod print-msg :default [input]
  (println (str "[msg] " (pr-str input) "\n")))


(defn dissoc-outputs [input]
  (dissoc input ::cmds ::effs))

(defn ->on-cmd-input [input command]
  (-> input dissoc-outputs (assoc cmd command)))

(defn ->on-msg-input [input message]
  (-> input dissoc-outputs (assoc ::msg message)))

(defn ->on-eff-input [input effect]
  (-> input dissoc-outputs (assoc ::eff effect)))

(defn ->on-evt-input [input event]
  (-> input dissoc-outputs (assoc ::evt event)))

;; 
;; 
;; 
;; 
;; 

(defn- stepper-events! [output events]
  (loop [running-output (dissoc output ::eff)
         modules @modules]
    (if (empty? modules)
        running-output
        (let [event (::evt (first events))
              output-from-event (on-evt (assoc input ::evt event ::module (first modules)))
              next-effects (concat (->effs running-output) (->effs output-from-event))
              next-commands (concat (->cmds running-output) (->cmds output-from-event))
              next-output (merge output-from-event {::effs next-effects ::cmds next-commands})] 
          (print-msg event)
          (recur next-output (rest modules))))))

(defn- stepper-commands! [output commands]
  (let [input (->on-cmd-input output (first commands))
        output-from-cmd (on-cmd input)
        next-effects (concat (->effs output) (->effs output-from-cmd))
        next-commands (concat (rest commands) (->cmds output-from-cmd))
        next-output (merge output-from-cmd {::effs next-effects ::cmds next-commands})]
    (print-msg input)
    next-output))

(defn- stepper-effects! [output effects]
  (let [input (->on-eff-input output (first effects))
        output-from-eff (on-eff! input)
        next-effects (concat (rest effects) (->effs output-from-eff))
        next-commands (concat (->cmds output) (->cmds output-from-eff))
        next-output (merge output-from-eff {::effs next-effects ::cmds next-commands})]
    (print-msg input)
    next-output))

(defn- stepper-recur! [output]
  (let [effects (->effs output) 
        commands (->cmds output)]
    (cond 
      (first commands) 
      (stepper-recur! (stepper-commands! output commands))
      
      (first effects)
      (stepper-recur! (stepper-effects! output effects))
      
      :else
      output)))

(defn- stepper! 
  [state message] 
  (let [input (->on-msg-input state message)]
    (print-msg input)
    (stepper-recur! (on-msg input))))
    
(defn step! 
  [state! message] 
  (reset! state! (stepper! @state! message)))

(defn init! [state!] 
  (reset! state! (stepper-recur! (on-init @state!))))
  