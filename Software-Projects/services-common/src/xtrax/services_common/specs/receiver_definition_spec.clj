(ns xtrax.services-common.specs.receiver-definition-spec
  (:require [clojure.alpha.spec :as s]
            [clojure.alpha.spec.gen :as gen]
            [clojure.alpha.spec.test :as test]
            [xtrax.services-common.protocols.receiver-definition
              :refer [ReceiverDefinitionProtocol ReceiverScheduleProtocol StreamSendEventsConfigurationProtocol
                      RepeatEveryProtocol RestRunOnProtocol RepeatOnProtocol EndsOnProtocol]]))

;;;;;
;;
;;  SCHEMA DEFINITION SEGMENT:
;;

; receiver-definition
; receiver-definition.signal-key
; receiver-definition.receiver-key
; receiver-definition.receiver-squuid
; receiver-definition.receiver-name
; receiver-definition.receiver-description
; receiver-definition.receiver-schedule
; receiver-definition.receiver-schedule.schedule-type
; receiver-definition.receiver-schedule.first-run-on
; receiver-definition.receiver-schedule.rest-run-on
; receiver-definition.receiver-schedule.rest-run-on.repeat-every
; receiver-definition.receiver-schedule.rest-run-on.repeat-every.value
; receiver-definition.receiver-schedule.rest-run-on.repeat-every.scale
; receiver-definition.receiver-schedule.rest-run-on.repeat-on
; receiver-definition.receiver-schedule.rest-run-on.repeat-on.repeat-day
; receiver-definition.receiver-schedule.rest-run-on.ends-on
; receiver-definition.receiver-schedule.rest-run-on.ends-on.end-type
; receiver-definition.receiver-schedule.rest-run-on.ends-on.on-date
; receiver-definition.stream-send-events-configuration
; receiver-definition.stream-send-events-configuration.requested-update-method
; receiver-definition.stream-send-events-configuration.requested-update-frequency


(s/def :receiver-schedule/schedule-type (s/and
                                          (and (keyword? %) (= :first-run-on/now %))
                                          (or
                                            (= :schedule-type/single-instance %)
                                            (= :schedule-type/continuous %))))
(s/def :receiver-schedule/first-run-on (or
                                         (instance? java.util.Date %)
                                         (= :first-run-on/now %)))
(s/def :receiver-schedule/rest-run-on (satisfies? RestRunOnProtocol %) )

(s/def :rest-run-on/repeat-every (satisfies? RepeatEveryProtocol %))
(s/def :rest-run-on/repeat-on (satisfies? RepeatOnProtocol %))
(s/def :rest-run-on/ends-on (satisfies? EndsOnProtocol %))


(s/def :repeat-every/value int?)
(s/def :repeat-every/scale #{:repeat-every/second
                             :repeat-every/minute
                             :repeat-every/hour
                             :repeat-every/day
                             :repeat-every/week
                             :repeat-every/month
                             :repeat-every/year})

(s/def :repeat-on/repeat-day #{:repeat-on/monday
                                :repeat-on/tuesday
                                :repeat-on/wednesday
                                :repeat-on/thursday
                                :repeat-on/friday
                                :repeat-on/saturday
                                :repeat-on/sunday})


(s/def :ends-on/end-type #{:ends-on/never :ends-on/on :ends-on/after})
(s/def :ends-on/on-date (instance? java.util.Date %))


(s/def :receiver-definition/signal-key keyword?)
(s/def :receiver-definition/receiver-key keyword?)
(s/def :receiver-definition/receiver-squuid (instance? java.util.UUID %))
(s/def :receiver-definition/receiver-name string?)
(s/def :receiver-definition/receiver-description string?)
(s/def :receiver-definition/receiver-schedule (satisfies? ReceiverScheduleProtocol %))
(s/def :receiver-definition/stream-send-events-configuration (satisfies? StreamSendEventsConfigurationProtocol %))



(s/def :receiver-schedule/schedule-type (s/and
                                          keyword?
                                          (or
                                            (= :schedule-type/single-instance %)
                                            (= :schedule-type/continuous %))))
(s/def :receiver-schedule/first-run-on (or
                                         (instance? java.util.Date %)
                                         (= :first-run-on/now %)))
(s/def :receiver-schedule/rest-run-on (satisfies? RestRunOnProtocol %))








(s/def :stream-send-events-configuration/requested-update-method #{:requested-update-method/time
                                                                   :requested-update-method/bytes-txd})
(s/def :stream-send-events-configuration/requested-update-frequency (or
                                                                      (int? %)
                                                                      (instance? java.time.Duration %)))
