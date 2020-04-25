(ns observideo.common.serde
  (:require
   [taoensso.timbre :as log]
   [cognitect.transit :as t]
   [clojure.walk :as walk]))

(def reader (t/reader :json))
(def writer (t/writer :json))

(defn- serialize [cljdata]
  (let [start  (.getTime (js/Date.))
        result (t/write writer cljdata)
        end    (.getTime (js/Date.))]
    (log/debug "Serialized in" (- end start) "ms")
    result))

(defn- deserialize [s]
  (let [start  (.getTime (js/Date.))
        result (t/read reader s)
        end    (.getTime (js/Date.))]
    (log/debug "Deserialized in" (- end start) "ms")
    result))
