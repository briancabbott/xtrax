(ns xtrax.service-orchestrator.db.data-store.provider.postgres_provider.postgres-datastore-provider
  (:require
    [xtrax.service-orchestrator.db.data-store.provider.data-store-provider :as dsp]
    [next.jdbc :as jdbc]
    [clojure.pprint :as pp]))


; (defprotocol IDataStoreProvider
;   (store-data [data-key data] "handles both create and update functions.")
;   (delete-data [data-key data] "performs a single delete operation for the id specified in data on the entity-type specified by data-key")
;   (read-data [data-key data]))

;
; (defprotocol P
;   (foo [this])
;   (bar-me [this] [this y]))
;
; (deftype Foo [a b c]
;  P
;   (foo [this] a)
;   (bar-me [this] b)
;   (bar-me [this y] (+ c y)))

(def postgres-connection (atom nil))

(defn create-postgres-connection []
  (if (not (nil? @postgres-connection))
    @postgres-connection
    (let [db-spec {:dbtype "postgresql"
                   :dbname "xtrax-dev-local"
                   :user "xtrax-brian-devdb"
                   :password "brian"
                   :host "localhost"
                   :port 5432}
          xtrax-datasource (jdbc/get-datasource db-spec)
          conn (. xtrax-datasource getConnection)
          md (. conn getMetaData)]
      (reset! postgres-connection conn)
      (prn "connnection")
      (pp/pprint conn)
      (prn "meta-data")
      (pp/pprint (.getNumericFunctions md))
      @postgres-connection)))


; (def data-type-handlers {:key fn})

(deftype PostgresDatastoreProvider []
  dsp/IDataStoreProvider
    (store-data [data-key data]
      (prn "store"))

    (delete-data [data-key data])

    (read-data [data-key data]))
