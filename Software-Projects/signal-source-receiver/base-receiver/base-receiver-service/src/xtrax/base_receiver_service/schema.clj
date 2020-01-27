(ns xtrax.base-receiver-service.schema
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :refer [resource]]

    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia.util :refer [attach-resolvers attach-scalar-transformers attach-streamers]]
    [com.walmartlabs.lacinia.pedestal :as lp]
    [xtrax.base-receiver-service.receiver :as receiver])
  (:import
    (java.text SimpleDateFormat)))


(def datetime-formatter (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ"))
(def date-formatter (SimpleDateFormat. "yyyy-MM-dd"))

(def datetime-transformers {:datetime-parser     #(when % (.parse date-formatter %))
                            :datetime-serializer #(when % (.format datetime-formatter %))
                            :date-parser         #(when % (.parse date-formatter %))
                            :date-serializer     #(when % (.format date-formatter %))})

;      (attach-scalar-transformers datetime-transformers)
; (attach-streamers {:subscriptions/subscribe-receive-signal receiver/subscribe-receive-signal})

(def load-graphql-schema
  (-> "graphql-definitions.edn"
      resource
      slurp
      edn/read-string
      (attach-scalar-transformers datetime-transformers)
      ; (attach-resolvers {:resolve-receive-signal receiver/subscribe-receive-signal})
      (attach-streamers {:subscribe-receiver-stream receiver/subscribe-receiver-stream})
      schema/compile
      (lp/service-map {:graphiql true
                       :path "/"
                       :ide-path "/ui"
                       :subscriptions true
                       :subscriptions-path "/graphql-ws"})))

      ; (attach-resolvers {:resolve-receive-signal receiver/resolve-receive-signal})
