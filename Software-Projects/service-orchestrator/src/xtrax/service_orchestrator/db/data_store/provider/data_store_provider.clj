(ns xtrax.service-orchestrator.db.data-store.provider.data-store-provider)

(defprotocol IDataStoreProvider
  (store-data [data-key data] "handles both create and update functions.")
  (delete-data [data-key data] "performs a single delete operation for the id specified in data on the entity-type specified by data-key")
  (read-data [data-key data]))
