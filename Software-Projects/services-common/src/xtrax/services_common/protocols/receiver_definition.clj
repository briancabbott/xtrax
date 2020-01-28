(ns xtrax.services-common.protocols.receiver-definition
  (:import [java.util Date]))


(declare ReceiverDefinitionProtocol)
(declare ReceiverScheduleProtocol)
(declare StreamSendEventsConfigurationProtocol)
(declare RestRunOnProtocol)
(declare RepeatEveryProtocol)
(declare RepeatOnProtocol)
(declare EndsOnProtocol)


(defprotocol ReceiverDefinitionProtocol
  (^String signal-key [this])
  (^Keyword receiver-key [this])
  (^Long receiver-squuid [this])
  (^String receiver-name [this])
  (^String receiver-description [this])
  (^ReceiverScheduleProtocol receiver-schedule [this])
  (^StreamSendEventsConfigurationProtocol stream-send-events-configuration [this]))

(deftype ReceiverDefinitionType [signal-key receiver-key receiver-squuid receiver-name
                                 receiver-description receiver-schedule
                                 stream-send-events-configuration]
  ReceiverDefinitionProtocol
  (signal-key [this] signal-key)
  (receiver-key [this] receiver-key)
  (receiver-squuid [this] receiver-squuid)
  (receiver-name [this] receiver-name)
  (receiver-description [this] receiver-description)
  (receiver-schedule [this] receiver-schedule)
  (stream-send-events-configuration [this] stream-send-events-configuration))

;;;;;;;
  ; Instantiating a record example:
  ;
  ; (def stu (Person. "Stu" "Halloway"
  ;            (Address. "200 N Mangum"
  ;                       "Durham"
  ;                       "NC"
  ;                       27701)))
  ;
(defrecord ReceiverDefinitionRecord [signal-key
                                     receiver-key
                                     receiver-squuid
                                     receiver-name
                                     receiver-description
                                     receiver-schedule
                                     stream-send-events-configuration])




:schedule-type/single-instance
:schedule-type/continuous
:first-run-on/now
(defprotocol ReceiverScheduleProtocol
  (^Keyword schedule-type [this])
  (first-run-on [this])
  (^RestRunOnProtocol rest-run-on [this]))

(deftype ReceiverScheduleType [schedule-type first-run-on rest-run-on]
  ReceiverScheduleProtocol
  (schedule-type [this] schedule-type)
  (first-run-on [this] first-run-on)
  (rest-run-on [this] rest-run-on))

(defrecord ReceiverScheduleRecord [schedule-type first-run-on rest-run-on])




:requested-update-method/time
:requested-update-method/bytes-txd
(defprotocol StreamSendEventsConfigurationProtocol
  (^Keyword requested-update-method [this]
    "Must be one of: :requested-update-method/time or :requested-update-method/bytes-txd")
  (requested-update-frequency [this]
    "A value relative to the method. When time, integers are in milliseconds or,
    optionally a default formats of java.time.Duration are available."))

(deftype StreamSendEventsConfigurationType [requested-update-method requested-update-frequency]
  StreamSendEventsConfigurationProtocol
  (requested-update-method [this] requested-update-method)
  (requested-update-frequency [this] requested-update-frequency))

(defrecord StreamSendEventsConfigurationRecord [requested-update-method requested-update-frequency])




(defprotocol RestRunOnProtocol
  (^RepeatEveryProtocol repeat-every [this])
  (^RepeatOnProtocol repeat-on [this])
  (^EndsOnProtocol ends-on [this]))

(deftype RestRunOnType [repeat-every repeat-on ends-on]
  RestRunOnProtocol
  (repeat-every [this] repeat-every)
  (repeat-on [this] repeat-on)
  (ends-on [this] ends-on))

(defrecord RestRunOnRecord [repeat-every repeat-on ends-on])




:repeat-every/second
:repeat-every/minute
:repeat-every/hour
:repeat-every/day
:repeat-every/week
:repeat-every/month
:repeat-every/year
(defprotocol RepeatEveryProtocol
  (^Long value [this])
  (^Keyword scale [this]))

(deftype RepeatEveryType [value scale]
  RepeatEveryProtocol
  (value [this] value)
  (scale [this] scale))

(defrecord RepeatEveryRecord [value scale])




:repeat-on/monday
:repeat-on/tuesday
:repeat-on/wednesday
:repeat-on/thursday
:repeat-on/friday
:repeat-on/saturday
:repeat-on/sunday
(defprotocol RepeatOnProtocol
  (^Keyword repeat-day [this]))

(deftype RepeatOnType [repeat-day]
  RepeatOnProtocol
  (repeat-day [this] repeat-day))

(defrecord RepeatOnType [repeat-day])




:ends-on/never
:ends-on/on
:ends-on/after
(defprotocol EndsOnProtocol
  (^Keyword end-type [this])
  (^Date on-date [this]))

(deftype EndsOnType [end-type on-date]
  EndsOnProtocol
  (end-type [this] end-type)
  (on-date [this] on-date))

(defrecord EndsOnRecord [end-type on-date])
