

# Clojure Spec (as spec.alpha2)

## Running Spec
Alpha2 was not integrated with the clojure distribute so, it is recommended to
run it directly from the git url via the following configuration:

clj -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.10.0"}
                    org.clojure/test.check {:mvn/version "0.9.0"}
                    org.clojure/spec-alpha2 {:git/url "https://github.com/clojure/spec-alpha2.git"
                                             :sha "<SHA>"}}}'




## Core Spec Function/Object Model:

(defprotocol Spec
  :extend-via-metadata true
  (conform* [spec x settings-key settings])
  (unform* [spec y])
  (explain* [spec path via in x settings-key settings])
  (gen* [spec overrides path rmap])
  (with-gen* [spec gfn])
  (describe* [spec]))

(defprotocol Schema
  (keyspecs* [spec] "Returns map of key to symbolic spec"))

(defprotocol Select
  "Marker protocol for selects")





## Specs found in source:

s/create-spec 'fn*
(defmethod s/create-spec `fn
  (defmethod s/create-spec 'fn
  (defmethod s/create-spec `s/with-gen
    (defmethod s/create-spec `s/conformer

      (defmethod s/create-spec `s/*
        (defmethod s/create-spec `s/+
          (defmethod s/create-spec `s/?
            (defmethod s/create-spec `s/alt
              (defmethod s/create-spec `s/cat
                (defmethod s/create-spec 'clojure.alpha.spec/&
                (defmethod s/create-spec 'clojure.alpha.spec/&
                (defmethod s/create-spec `s/keys
                  (defmethod s/create-spec `s/schema
                    (defmethod s/create-spec `s/union
                      (defmethod s/create-spec `s/select
                        (defmethod s/create-spec `s/nest
                          (defmethod s/create-spec `s/multi-spec
                            (defmethod s/create-spec `s/tuple
                              (defmethod s/create-spec `s/or


                                (defmethod s/create-spec `s/and
                                  (defmethod s/create-spec `s/and
                                    (defmethod s/create-spec `s/and-
                                      (defmethod s/create-spec `s/merge
                                        (defmethod s/create-spec `s/every
                                          (defmethod s/create-spec `s/every-kv
                                            (defmethod s/create-spec `s/coll-of
                                              (defmethod s/create-spec `s/map-of
                                                (defmethod s/create-spec `s/keys*
                                                  (defmethod s/create-spec `s/fspec
                                                    (defmethod s/create-spec `s/nonconforming
                                                      (defmethod s/create-spec `s/nilable
