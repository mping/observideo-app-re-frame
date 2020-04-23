(ns observideo.common.serde
  (:require
   [taoensso.timbre :as log]
   [cognitect.transit :as t]
   [clojure.walk :as walk]))

(def reader (t/reader :json))
(def writer (t/writer :json))

(defn- serialize [cljdata] (t/write writer (walk/keywordize-keys cljdata)))
(defn- deserialize [s] (t/read reader s))
