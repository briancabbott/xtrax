(ns xtrax.base-receiver-service.receiver
  (:require
    [clojure.core.async :refer [chan close! go alt! timeout]]))

(def stream-send-events (atom 0))

(defn start-receiver [a]
  (reset! stream-send-events 0)
  0)

(defn has-receiver-sent-close-event? [receiver-watch-object]
  (prn (str " in ----> has-receiver-sent-close-event?::  "  @stream-send-events))
  (swap! stream-send-events inc)
  (if (< @stream-send-events 30)
    false
    true))

(defn subscribe-receiver-stream [context args source-stream]
  (let [sigdef-arg (:sigdef args)
        signal-key (:signalKey sigdef-arg)
        receiver-watch-obj (start-receiver signal-key)]
    (let [abort-ch (chan)]
      (go
        (loop [stream-active? (not (has-receiver-sent-close-event? receiver-watch-obj))]
          (prn "LOOPING!")
          (if stream-active?
            (do
              ;; To-do: Block and wait until the next stream-event comes in from the receiver?
              (source-stream {:signalKey @stream-send-events})
              (alt! abort-ch nil (timeout 1000)
              (recur (not (has-receiver-sent-close-event? receiver-watch-obj)))))
            (source-stream nil))))
      ;; Cleanup:
      #(close! abort-ch))))
