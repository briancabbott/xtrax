(ns xtrax.services-common.squuid)

(defn gen-squuid []
  "Generates a Datomic SQUUID - A Sequential UUID"
  (let [uuid (java.util.UUID/randomUUID)
        time (System/currentTimeMillis)
        secs (quot time 1000)
        lsb (.getLeastSignificantBits uuid)
        msb (.getMostSignificantBits uuid)
        timed-msb (bit-or (bit-shift-left secs 32)
                          (bit-and 0x00000000ffffffff msb))
        generated-squuid (java.util.UUID. timed-msb lsb)]
    generated-squuid))
