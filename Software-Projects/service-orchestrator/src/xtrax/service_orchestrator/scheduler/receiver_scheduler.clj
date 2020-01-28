(ns xtrax.service-orchestrator.scheduler.receiver-scheduler
  (:require [clj-time.types :refer [date-time?]]
            [xtrax.service-orchestrator.receiver-coordinator :as coordinator]
            [xtrax.service-orchestrator.db.data-store.core :as data-store]))
;
; ;; Receiver-Schedules objects
; [{:signal-key "test-receiver"
;   :receiver-schedule {:schedule-type (:single-instance | :continuous)
;                       :first-run-on (:now | DateTimeObject) ;; Optional when type is continuous...
;                       ;; Only valid for :continuous operations
;                       :rest-run-on {:repeat-every {:value 1 :scale (:second | :minute | :hour | :day | :week | :month | :year)}
;                                     :repeat-on (:monday :tuesday :wednesday :thursday :friday :saturday :sunday)
;                                     :ends-on (:never {:on DateTime Object}  {:after DateTimeObject})}}}]
;

(declare is-receiver-schedule-valid?)

(defn create-new-receiver-schedule
  "Create a new Receiver-Schedule Record in the configured Datra-Store and, if
  its first start-time is within the current orchestration-executor's
  scheduling-period, start it."
  [receiver-schedule-object]
  (if (is-receiver-schedule-valid? receiver-schedule-object)
    (do
      (data-store/persist-object :receiver-schedule-object receiver-schedule-object)
      (coordinator/check-execute-on-scheduler receiver-schedule-object))
    (do
      ;; throw an exception
      (throw (ex-info "Recevier-Schedule Object failed validation" {})))))


(defn is-receiver-schedule-valid? [receiver-schedule-object]
   (if (not (nil? receiver-schedule-object))
     (let [receiver-schedule (:receiver-schedule receiver-schedule-object)
           schedule-type (:schedule-type receiver-schedule)
           first-run (:first-run-on receiver-schedule)
           rest-run (:rest-run-on receiver-schedule)
           repeat-scale (-> rest-run :repeat-every :scale)
           repeat-value (-> rest-run :repeat-every :value)
           repeat-on (-> rest-run :repeat-on)
           repeat-scale-valid? (or
                                 (= repeat-scale :second)
                                 (= repeat-scale :minute)
                                 (= repeat-scale :hour)
                                 (= repeat-scale :day)
                                 (= repeat-scale :week)
                                 (= repeat-scale :month)
                                 (= repeat-scale :year))
           repeat-value-valid? (and repeat-scale-valid? (> repeat-value 0))
           ends-on (-> rest-run :ends-on)
           ends-on-has-on-term? (and (map? ends-on) (contains? ends-on :on) (date-time? (:on ends-on)))
           ends-on-has-after-term? (and (map? ends-on) (contains? ends-on :after) (date-time? (:after ends-on)))]
      (let [schedule-type-valid? (or (= schedule-type :single-instance) (= schedule-type :continuous))
            first-run-on-valid? (or (= first-run :now) (date-time? first-run))
            repeat-on-valid? (and
                               (and
                                 (not repeat-scale-valid?)
                                 (not repeat-value-valid?))
                               (or (= repeat-on :monday)
                                   (= repeat-on :tuesday)
                                   (= repeat-on :wednesday)
                                   (= repeat-on :thursday)
                                   (= repeat-on :friday)
                                   (= repeat-on :saturday)
                                   (= repeat-on :sunday)))
            ends-on-valid? (or
                             (and
                               (= ends-on :never)
                               (not ends-on-has-on-term?)
                               (not ends-on-has-after-term?))
                             (and
                               ends-on-has-on-term?
                               (not ends-on-has-after-term?))
                             (and
                               ends-on-has-after-term?
                               (not ends-on-has-on-term?)))]
           (and schedule-type-valid? first-run-on-valid? repeat-on-valid? ends-on-valid?)))
     false))
