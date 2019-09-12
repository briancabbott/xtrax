(ns rssp-google-research-paper-publication.service
  (:require [clojure.pprint :as pp]

            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]

            [clojure.java.io :as io]
            [clojure.string :as str]

            [ring.util.response :as ring-resp]

            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.local :as tl]
            [clj-time.coerce :as tc]

            [clojure.data.xml :refer :all]))


;; Config Vars:
(def GOOGLE-AI-PUBS-DATASET-URI "https://storage.googleapis.com/pub-tools-public-publication-data/")
(def PROCESSED-ELEMENT-DATASTORE "data-store/__processed-store")
(def OPERATIONS-ELEMENT-DATASTORE "data-store/ops-store")

(defn- load-config []
  )

(defn ensure-storage-is-online [operation-name]
  (let [op-dir (str "./data-store/ops-store/" operation-name)
        op-dir-file (java.io.File. op-dir)
        op-dir-exists (.exists op-dir-file)
        op-dir-created (.mkdir op-dir-file)]
    (prn (str "op-dir-exists: " op-dir-exists))
    (prn (str "op-dir-created: " op-dir-created))
    (.getAbsolutePath op-dir-file)
    ))

(defn save-operation-results-to-datastore [storage-path edn-results-filename xtracted-data-lst]
  ;; pretty-prints into a string buffer and then saves the string-buffer to a file.
  (let [out (java.io.StringWriter.)]
    (clojure.pprint/pprint xtracted-data-lst out)
    (let [pp-edn-results (.toString out)]
      (spit (str storage-path "/" edn-results-filename) pp-edn-results))))

(defn download-publications-dataset-xml [uri temp-file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream temp-file)]
    (io/copy in out)
    (let [file-written? (.exists (io/file temp-file))]
      (if file-written?
        {:status :ok}
        {:status :failed}))))




        ; IS_NAMESPACE_AWARE
        ; public static final String IS_NAMESPACE_AWARE
        ; The property used to turn on/off namespace support, this is to support XML 1.0 documents, only the true setting must be supported
        ; See Also:
        ; Constant Field Values
        ; IS_VALIDATING
        ; public static final String IS_VALIDATING
        ; The property used to turn on/off implementation specific validation
        ; See Also:
        ; Constant Field Values
        ; IS_COALESCING
        ; public static final String IS_COALESCING
        ; The property that requires the parser to coalesce adjacent character data sections
        ; See Also:
        ; Constant Field Values
        ; IS_REPLACING_ENTITY_REFERENCES
        ; public static final String IS_REPLACING_ENTITY_REFERENCES
        ; Requires the parser to replace internal entity references with their replacement text and report them as characters
        ; See Also:
        ; Constant Field Values
        ; IS_SUPPORTING_EXTERNAL_ENTITIES
        ; public static final String IS_SUPPORTING_EXTERNAL_ENTITIES
        ; The property that requires the parser to resolve external parsed entities
        ; See Also:
        ; Constant Field Values
        ; SUPPORT_DTD
        ; public static final String SUPPORT_DTD
        ; The property that requires the parser to support DTDs
        ; See Also:
        ; Constant Field Values
        ; REPORTER
        ; public static final String REPORTER
        ; The property used to set/get the implementation of the XMLReporter interface
        ; See Also:
        ; Constant Field Values
        ; RESOLVER
        ; public static final String RESOLVER
        ; The property used to set/get the implementation of the XMLResolver
        ; See Also:
        ; Constant Field Values
        ; ALLOCATOR
        ; public static final String ALLOCATOR
        ; The property used to set/get the implementation of the allocator
        ; See Also:
        ; Constant Field Values
        ;

(defn get-raw-signal-data [request]
  (let [uri GOOGLE-AI-PUBS-DATASET-URI
        operation-timestamp-str (tf/unparse (tf/formatters :basic-date-time) (t/now))
        operation-name (str "google-ai--pub-tools-public-publication-data--" operation-timestamp-str)
        storage-path (ensure-storage-is-online operation-name)
        timestamp-now (tl/local-now)
        time-formatter (tf/formatter "MM-dd-yyyy")
        mdt-as-str (tf/unparse time-formatter timestamp-now)
        temp-file (str "ai-google-com-pubs-rawxml-" mdt-as-str ".xml")
        edn-results-file (str "ai-google-com-pubs-operationedn-" operation-timestamp-str ".edn")]

    (let [op-status (download-publications-dataset-xml uri temp-file)
          xtracted-data-lst (atom [])
          mapt-data-hashset (atom {})] ;; manifest, authors, publications, tags

      (if (= (:status op-status) :ok) (do
        ;; next steps....
        (let [raw-xml (slurp temp-file)
              parsed-xml (clojure.data.xml/parse-str raw-xml)]

          (if (not= (:tag parsed-xml) :ListBucketResult)
            (do ;; Error, structure possibly changed? Continue?--perhaps-a-setting
              {:status :bad}
              )
            (do ;; else (continue operating...)
              (let [content-list (:content parsed-xml)]
                (doall (for [content-item content-list] (do
                  (cond
                    (= (:tag content-item) :Name) (do
                      (prn "Name tag")
                      (:content content-item)
                      )
                    (= (:tag content-item) :Prefix) (do
                      (prn "Prefix tag")
                      )
                    (= (:tag content-item) :Marker) (do
                      (prn "Marker tag")
                      )
                    (= (:tag content-item) :NextMarker) (do
                      (prn "NextMarker tag")
                      )
                    (= (:tag content-item) :IsTruncated) (do
                      (prn "IsTruncated tag")
                      )
                    (= (:tag content-item) :Contents) (do
                      (let [content-record (:content content-item)
                            field-uri (atom nil)
                            field-timestamp (atom nil)
                            field-meta (atom nil)
                            field-lastmodified (atom nil)
                            field-etag (atom nil)
                            field-size (atom nil)]
                        (doall (for [field content-record] (do
                          (cond
                            ;; 1568014489 - epoch
                            ;; 1568014489162 - millis
                               ;; 1523024112390857 -- from Google Pubs doc
                            (= (:tag field) :Key) (do
                              (let [raw-url (first (:content field))]
                                (prn (str "raw-url type: " raw-url))
                                (reset! field-uri raw-url)
                              )
                              )
                            (= (:tag field) :Generation) (do
                              (let [raw-timestamp (first (:content field))
                                    parsed-timestamp (.longValue (java.math.BigInteger. raw-timestamp))
                                    div-timestamp (quot parsed-timestamp 1000)
                                    ; parsed-epoch (. Long parseLong raw-timestamp)
                                    coerced-timestamp (tc/from-long div-timestamp)]
                                ; (prn (str "div-time: " div-timestamp))
                                ; (prn (str "coerced-timestamp type: " coerced-timestamp))
                                (reset! field-timestamp coerced-timestamp)
                                )
                              )
                            (= (:tag field) :MetaGeneration) (do
                              (let [raw-meta (first (:content field))
                                    parsed-meta (. Integer parseInt raw-meta)]
                                ; (prn (str "raw-meta type: " (type raw-meta)))
                                (reset! field-meta parsed-meta)
                                )
                              )
                            (= (:tag field) :LastModified) (do
                              (let [raw-lastmodified (first (:content field))
                                    coerced-lastmodified (tc/from-string raw-lastmodified)]
                                ; (prn (str "raw-lastmodified type: " (type raw-lastmodified)))
                                ; 2018-04-06T14:15:15.124Z
                                (reset! field-lastmodified coerced-lastmodified)
                                )
                              )
                            (= (:tag field) :ETag) (do
                              (let [raw-etag (first (:content field))]
                                ; (prn (str "raw-etag type: " (type raw-etag)))
                                (reset! field-etag raw-etag)
                                )
                              )
                            (= (:tag field) :Size) (do
                              (let [raw-size (first (:content field))
                                    parsed-size (. Integer parseInt raw-size)]
                                ; (prn (str "raw-size type: " (type raw-size)))
                                (reset! field-size parsed-size)
                                )
                              )
                          )
                        ))) ; end-for

                        ; TODO: Add an exclusions list to our config:
                        ;; (if (not in exclusions-list))
                        ;; TMP Exclusion: pdf/
                        (if (not= @field-uri "pdf/") (do
                          (let [data-element { :base-uri GOOGLE-AI-PUBS-DATASET-URI
                                               :file-path-uri @field-uri
                                               :timestamp @field-timestamp
                                               :meta @field-meta
                                               :lastmodified @field-lastmodified
                                               :etag @field-etag
                                               :size @field-size }]
                            (if (str/ends-with? (:file-path-uri data-element) "json") (do
                              (let [str-vec (str/split (:file-path-uri data-element) #"/")
                                    date-element (nth str-vec 0)
                                    formatted-date (str/replace date-element ":" "-")
                                    date-key (keyword formatted-date)]
                                (if (not (contains? @mapt-data-hashset date-key)) (do
                                  (swap! mapt-data-hashset assoc date-key [])))
                                (let [key-vec (date-key @mapt-data-hashset)
                                      new-vec (conj key-vec data-element)]
                                  (swap! mapt-data-hashset assoc date-key new-vec))))
                            (do ;; else (regular case -- an ordinary data-element to be added to the list)
                              (swap! xtracted-data-lst conj data-element)))))) ;; end-if
                               ) ; end-let
                              ) ; end-do
                          ) ; end cond

                        )))

                        )) ; if-else
                        ) ; if ListBucketResult
                        )
                        )
                        ) ; if (:status op-status) :ok)

        ;; Save in the MAPT links before we persist the operation:
        (save-operation-results-to-datastore storage-path edn-results-file
           {:operation-name operation-name
            :operation-timestamp operation-timestamp-str
            :mapt-dataset @mapt-data-hashset
            :public-papers-dataset @xtracted-data-lst})

        ;; Generate and return the response message:
        ;;   TODO: Add a url where the newly created dataset can be found
        { :status 200
          :body "operation complete."
        }

        )))

(defn get-service-operation-dataset-list [request]

  )
(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]

              ["/about" :get (conj common-interceptors `about-page)]

              ;; Execute a service-operation, to aquire a new raw data-set, which can then be processed into refined signals.
              ["/service-exec" :get (conj common-interceptors `get-raw-signal-data)]

              ;; The list of datasets generated from service-operations runs
              ["/service-operation-dataset-list" :get (conj common-interceptors `get-service-operation-dataset-list)]

              })

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by rssp-google-research-paper-publication.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false
                                        ;; Alternatively, You can specify you're own Jetty HTTPConfiguration
                                        ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)
                                        }})
