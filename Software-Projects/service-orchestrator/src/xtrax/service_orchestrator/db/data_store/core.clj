(ns xtrax.service-coordinator.db.data-store.core)

(defprotocol IDataStore )


(defn connect-store [])

(defn disconnect-store [])

(defn reconnect-store [])

(defn test-store-connection [])

(defn get-store-connection-metadata
  "Data about both the connection-status and the DataStore as well."
  [])



(defn persist-entity [object-key object])
