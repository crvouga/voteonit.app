(ns core)

;; 
;; 
;; 
;; 
;; 
;; 

(defmulti handle-msg (fn [input] (-> input :msg :type)))

(defmulti handle-command (fn [input] (-> input :command :type)))

(defmulti handle-effect! (fn [input] (-> input :effect :type)))

;; 
;; 
;; 
;; 
;; 

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

(defn ->state [output] 
  (-> output :state))

(defn print-msg [input]
  (let [command (-> input :command)
        msg (-> input :msg)
        effect (-> input :effect)]
    (when command (println (str "[command] " (pr-str command) "\n")))
    (when msg (println (str "[msg] " (pr-str msg) "\n")))
    (when effect (println (str "[effect] " (pr-str effect) "\n")))))

(defn step! [input]
  (print-msg input)
  (let [output-from-msg (handle-msg input)]
    (loop [running-output output-from-msg]
     (let [effects (->effects running-output)
           commands (->commands running-output)
           state (or (->state running-output) (->state output-from-msg))]
       (cond 
         (first commands) 
         (let [input {:state state :command (first commands)}
               output-from-command (handle-command input)
               output-next {:state (or (->state output-from-command) state)
                            :effects (concat effects (->effects output-from-command))
                            :commands (concat (rest commands) (->commands output-from-command))}]
           (print-msg input)
           (recur output-next))
         
         (first effects)
         (let [input {:state state :effect (first effects)}
               output-from-effect (handle-effect! input)
               output-next {:state (or (->state output-from-effect) state)
                            :effects (concat (rest effects) (->effects output-from-effect))
                            :commands (concat commands (->commands output-from-effect))}]
           (print-msg input)
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
  (append-effect input {:type ::publish :msg event}))

(def event-handlers (atom #{}))

(defn register-event-handler! [handle-event]
  (swap! event-handlers conj handle-event))

(defmethod handle-effect! ::publish [input]
  (loop [running-output {:state (:state input)}
         event-handlers @event-handlers]
    (if (empty? event-handlers)
        running-output
        (let [handle-event (first event-handlers)
              msg  (-> input :effect :msg)
              output-from-event (handle-event (assoc input :msg msg))
              output-next {:state (-> output-from-event :state)
                           :effects (concat (->effects running-output) (->effects output-from-event))
                           :commands (concat (->commands running-output) (->commands output-from-event))}] 
          
          (recur output-next (rest event-handlers))))))