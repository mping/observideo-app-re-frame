(ns observideo.common.utils
  (:require [clojure.string :as s]))

(defn fname [path]
  ;;TODO use os.separator
  (subs path (inc (s/last-index-of path "/"))))


(defn relname [basedir path]
  (subs path (inc (count basedir))))
