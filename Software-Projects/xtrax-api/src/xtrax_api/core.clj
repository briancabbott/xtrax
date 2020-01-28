(ns xtrax-api.core
  (:gen-class))


  (def CONFIGURATION
	  {:app         {:command     "xtrax"
	                 :description "CLI Client-Interface to the Xtrax Application-Environment/Platform."
	                 :version     "0.0.1"}

	   ; :global-opts [{:option  "base"
	   ;                :as      "The number base for output"
	   ;                :type    :int
	   ;                :default 10}]

	   :commands    [{:command     "so"
	                  :description "Service-Orchestrator commands."
	                  :opts        [{:option "a" :as "Addendum 1" :type :int}
	                                {:option "b" :as "Addendum 2" :type :int :default 0}]
	                  :runs        #(prn "hello")
                    :subcommands [{:subcommand "create-receiver"
                                   :description ""
                                   :opts [{:option }]}

                                  ]}

	                 ; {:command     "sub"
	                 ;  :description "Subtracts parameter B from A"
	                 ;  :opts        [{:option "a" :as "Parameter A" :type :int :default 0}
	                 ;                {:option "b" :as "Parameter B" :type :int :default 0}]
	                 ;  :runs        subtract_numbers}
	                 ]})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
