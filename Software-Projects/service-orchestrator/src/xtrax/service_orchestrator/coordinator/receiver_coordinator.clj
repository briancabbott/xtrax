(ns xtrax.service-orchestrator.coordinator.receiver-coordinator
  (:require
    [chime :refer [chime-at]]
    [xtrax.service-orchestrator.db.data-store.provider.postgres_provider.postgres-datastore-provider :as pdp])
  (:import [java.time Instant Duration]))

;;; Responsible for coordinating executions-operations against the set of actions that a given
;;; operation might have to perform (receivers should, call back to this component - or the
;;; stream subscriber with their current state such that the coordinator can make sure that
;;; any dedicated resources required by the receiver are available
;;;
;;; A most simple example of the coordinators behavior would be to make sure if a receiver
;;; type is still running within its Freq window, not to start another one.
(declare process-scheduler-tick)

(defn start-receiver []
  (prn "starting up a receiver..."))

(defn periodic-seq [^Instant start duration-or-period]
  (iterate #(.addTo duration-or-period %) start))

(defn start-scheduler []
  (chime-at (-> (periodic-seq (Instant/now) (Duration/ofSeconds 5)) rest)
            (fn [time]
              (process-scheduler-tick time)
              (println "Chiming at " time))))
            ; {:on-finished (fn []
            ;                 (println "Schedule finished."))}))

(defn process-scheduler-tick [time]
  ; create scheduler-tick record: timestamp of tick, schedules found for exectution within frequency interval
  (pdp/create-postgres-connection)


  {:tick-timestamp ""
   :next-tick-timestamp ""
   :schedules-found-for-interval ["id-1", "id-2"]})

(defn check-execute-on-scheduler [receiver-schedule-object]
  ;; Find the needed execution start-time, if it is before the next scheduler-tick, instert it into the current set of receiver-objects set to be executed
  (prn (str " check-execute-on-scheduler")))
