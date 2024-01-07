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
  (reduce 
   (fn [acc module] (merge acc (on-init (assoc acc ::module module))))
   {}
   @modules))

(defmethod on-cmd :default [input]
  (println "Unhandled cmd" (cmd input))
  input)

(defmethod on-msg :default  [input]
  (println "Unhandled msg" (msg input))
  input)

(defmethod on-evt :default [input]
  (println "Unhandled event" (evt input))
  input)

(defmethod msgs! nil [input] 
  (doseq [module @modules]
    (msgs! (assoc input ::module module))))

(defmethod msgs! :default [input] 
  (println "Unhandled msgs!" (module input)))

(defmethod on-eff! :default [input]
  (println "Unhandled eff!" (eff input))
  input)

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

(defn- ->evts [output]
  (or (-> output ::evts seq) []))


;; 
;; 
;; 
;; 
;; 
;; 
;; 

(defn add-eff 
  ([input effect-type] 
   (add-eff input effect-type {}))
  
  ([input effect-type effect-payload]
   (let [eff-new (merge {::eff effect-type} effect-payload)
         effs-prev (->effs input)
         effs-new (conj effs-prev eff-new)
         output (assoc input ::effs effs-new)]
     output)))

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

(defn ->on-cmd-input [input cmd]
  (-> input 
      dissoc-outputs 
      (dissoc ::msg ::eff ::evt) 
      (merge cmd)))

(defn ->on-msg-input [input msg]
  (-> input 
      dissoc-outputs 
      (dissoc ::cmd ::eff ::evt)
      (merge msg)))

(defn ->on-eff-input [input eff]
  (-> input 
      dissoc-outputs 
      (dissoc ::cmd ::msg ::evt)
      (merge eff)))

(defn ->on-evt-input [input evt]
  (-> input 
      dissoc-outputs 
      (dissoc ::cmd ::msg ::eff)
      (merge evt)))

;; 
;; 
;; 
;; 
;; 

  
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
    #_(println "stepper-effs!" (select-keys input (keys (first effs))))
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
  [state message] 
  (let [input (->on-msg-input state message)
        output-from-msg (on-msg input)]
    (print-msg input)
    (stepper-recur! output-from-msg)))
    
(defn step! 
  [state! message] 
  (reset! state! (stepper! @state! message)))

(defn init! [state!] 
  (reset! state! (stepper-recur! (on-init @state!))))
  