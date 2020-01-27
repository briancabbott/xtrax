(ns xtrax.service-orchestrator.model.model)

(def config {:scheduler-tick-freq {:freq :scale}})


(def scheduler-tick-instance {:tick-timestamp ""
                              :next-tick-timestamp ""
                              :schedules-found-for-interval ["id-1", "id-2"]})

(def signal-definition {:signal-key ""
                        :signal-squuid ""
                        :signal-url ""
                        :signal-name ""
                        :signal-description ""
                        :signal-dataset-manual-description {:file-format '(xml, text, rss, html, json)
                                                            :is-compressed? ""
                                                            :compression-format ""}
                        :signal-dataset-statistically-generated-description {}})

(def receiver-definition {:signal-key "test-receiver"
                          :receiver-key ""
                          :receiver-squuid ""
                          :receiver-name ""
                          :receiver-description ""
                          :receiver-schedule {:schedule-type (:single-instance :continuous)
                                              :first-run-on (:now :DateTimeObject) ;; Optional when type is continuous...
                                              ;; Only valid for :continuous operations
                                              :rest-run-on {:repeat-every {:value 1 :scale '(:second :minute :hour :day :week :month :year)}
                                                            :repeat-on '(:monday :tuesday :wednesday :thursday :friday :saturday :sunday)
                                                            :ends-on '(:never {:on :DateTimeObject}  {:after :DateTimeObject})}}

                           :stream-send-events-configuration {:requested-update-method '(:time :bytes-txd) ;;;(:time | :bytes-txd)
                                                              :requested-update-frequency "value relative to the method"}})


(def receiver-execution-instance-record {:execution-instance-id {:signal-key ""
                                                                 :signal-instance-squuid ""
                                                                 :receiver-key ""
                                                                 :receiver-squuid ""}
                                         :receiver-signal-init {:signal-aq-start-time ""
                                                                :signal-payload-size ""
                                                                :signal-payload-last-modified-time ""
                                                                :signal-payload-response-headers ""
                                                                :signal-payload-transfer-protocol ""
                                                                :signal-payload-identified-artifacts-map ""
                                                                ;; How should the frequency for stream-event be
                                                                ;;   - note that this is duplicated as it could change in the definition after the receiver has ran
                                                                :stream-send-events-configuration {:requested-update-method "" ;;;(:time | :bytes-txd)
                                                                                                   :requested-update-frequency ""}}
                                         :receiver-signal-updates [{:signal-update-squuid    ""
                                                                    :signal-aq-update-time     "" ;"A timestamp of this update operation."}
                                                                    :bytes-received-since-last "" ; "the number of bytes received since the last update."}
                                                                    :bytes-received-total      "" ; "the total number of bytes received. This helps to ensure/signal state-consistency between the scheduler/actuator and the receiver"}
                                                                    :errors-in-this-cycle      " "}]  ;(list :ExceptionMessage) :description ""}}}]})
                                         :receive-signal-close {:signal-aq-end-time ""
                                                                :total-download-size ""
                                                                :status {:success '()
                                                                         :failure '()}}})
                                                                                     ;; (time-interval or, a value of the number of bytes received since the last update)}}
;
