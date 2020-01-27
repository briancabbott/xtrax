(ns xtrax.service_orchestrator.db.data.temp-data)


(def receiver-execution-schedules
  ;; Receiver-Schedules objects
  [{:signal-key "test-receiver"
    :receiver-schedule {:schedule-type '(:single-instance :continuous)
                        :first-run-on '(:now  :DateTimeObject)
                        ;; Only valid for :continuous operations
                        :rest-run-on {:repeat-every {:value 1 :scale '(:second  :minute  :hour  :day  :week  :month  :year)}
                                      :repeat-on '(:monday :tuesday :wednesday :thursday :friday :saturday :sunday)
                                      :ends-on '(:never {:on :DateTimeObject}  {:after :DateTimeObject})}}}])


(def receiver-instance-record {:receiver-instance-squuid "9838ufaofh"})
