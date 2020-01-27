(defproject base-receiver-service "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.pedestal/pedestal.service "0.5.8-SNAPSHOT"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.5.8-SNAPSHOT"]

                 ;; GraphQL provider
                 [com.walmartlabs/lacinia-pedestal "0.13.0-alpha-1"]


                 ;; [io.pedestal/pedestal.immutant "0.5.7"]
                 ;; [io.pedestal/pedestal.tomcat "0.5.7"]
                 [criterium "0.4.4"]
                 [expound "0.7.1"]
                 [joda-time "2.10"]
                 [com.walmartlabs/test-reporting "0.1.0"]
                 [io.aviso/logging "0.3.1"]
                 [io.pedestal/pedestal.log "0.5.8-SNAPSHOT"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/tools.cli "0.3.7"]


                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.26"]
                 [org.slf4j/jcl-over-slf4j "1.7.26"]
                 [org.slf4j/log4j-over-slf4j "1.7.26"]]

  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]

  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.5"]]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "base-receiver-service.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.7"]]}
             :uberjar {:aot [xtrax.base-receiver-service.server]}}
  :main ^{:skip-aot true} xtrax.base-receiver-service.server)
