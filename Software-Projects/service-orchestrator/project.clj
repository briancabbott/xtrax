(defproject service-orchestrator "0.0.1-SNAPSHOT"
  :description "FIXME: write description"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}


  :dependencies  [[clj-time/clj-time "0.15.3-SNAPSHOT"]
                  [org.clojure/clojure "1.10.1"]
                  [org.postgresql/postgresql "42.2.9.jre7"]
                  [seancorfield/next.jdbc "1.0.13"]

                  [re-graph "0.1.11"]

                  ;; Remove this line and uncomment one of the next lines to
                  ;; use Immutant or Tomcat instead of Jetty:
                  [io.pedestal/pedestal.jetty "0.5.7"]
                  [io.pedestal/pedestal.service "0.5.7"]
                  [org.eclipse.jetty/jetty-util "9.4.18.v20190429"]

                  ;; [io.pedestal/pedestal.immutant "0.5.7"]
                  ;; [io.pedestal/pedestal.tomcat "0.5.7"]
                  [criterium "0.4.4"]
                  [expound "0.7.1"]
                  [clj-antlr "0.2.4"]

                  [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                  [org.slf4j/jul-to-slf4j "1.7.26"]
                  [org.slf4j/jcl-over-slf4j "1.7.26"]
                  [org.slf4j/log4j-over-slf4j "1.7.26"]
                  [jarohen/chime "0.3.0-SNAPSHOT"]]

  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]

  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.5"]]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "service-orchestrator.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.7"]]}
             :uberjar {:aot [xtrax.service-orchestrator.server]}}
  :main ^{:skip-aot true} xtrax.service-orchestrator.server)
