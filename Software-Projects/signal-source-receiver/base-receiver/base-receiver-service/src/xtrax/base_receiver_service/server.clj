(ns xtrax.base-receiver-service.server
  ; (:gen-class)
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :refer [resource]]
    [xtrax.base-receiver-service.schema :as receiver-schema]
    [xtrax.base-receiver-service.receiver :as receiver]
    [com.walmartlabs.lacinia.schema :as schema]

    [io.pedestal.http :as server]
    [io.pedestal.http.route :as route]
    [xtrax.base-receiver-service.service :as service]
    ; [xtrax.base-receiver-service.schema :as schema]
    [com.walmartlabs.lacinia.pedestal :as lp]
    [com.walmartlabs.lacinia.util :refer [attach-resolvers attach-scalar-transformers attach-streamers]])
  (:gen-class))


;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
;
; (def signal-receiver-service (lp/service-map schema/load-graphql-schema
;                                             {:graphiql true
;                                               :path "/"
;                                               :ide-path "/ui"
;                                               :subscriptions true
;                                               :subscriptions-path "/graphql-ws"}))


(defonce runnable-service (server/create-server service/service))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  ; (prn (str "schema: " schema/load-graphql-schema))

  (-> service/service ;; start with production configuration
      (merge {:env :dev
              ;; do not block thread that starts web server
              ::server/join? false

              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::server/routes #(route/expand-routes (deref #'service/routes))

              ;; all origins are allowed in dev mode
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}

              ;; Content Security Policy (CSP) is mostly turned off in dev mode
              ::server/secure-headers {:content-security-policy-settings {:object-src "'none'"}}})

      ;; Wire up interceptor chains
      server/default-interceptors
      server/dev-interceptors
      receiver-schema/load-graphql-schema
      server/create-server
      server/start))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (-> "graphql-definitions.edn"
      resource
      slurp
      edn/read-string
      (attach-scalar-transformers receiver-schema/datetime-transformers)
      (attach-resolvers {:resolve-receive-signal receiver/subscribe-receive-signal})
      (attach-streamers {:subscriptions/subscribe-receive-signal receiver/receiver-activity-stream})
      schema/compile
      (lp/service-map {:graphiql true
                       :path "/"
                       :ide-path "/ui"
                       :subscriptions true
                       :subscriptions-path "/graphql-ws"})
      server/create-server
      server/start)
  (server/start runnable-service))

;; If you package the service up as a WAR,
;; some form of the following function sections is required (for io.pedestal.servlet.ClojureVarServlet).

;;(defonce servlet  (atom nil))
;;
;;(defn servlet-init
;;  [_ config]
;;  ;; Initialize your app here.
;;  (reset! servlet  (server/servlet-init service/service nil)))
;;
;;(defn servlet-service
;;  [_ request response]
;;  (server/servlet-service @servlet request response))
;;
;;(defn servlet-destroy
;;  [_]
;;  (server/servlet-destroy @servlet)
;;  (reset! servlet nil))
