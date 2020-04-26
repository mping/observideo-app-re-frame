(ns observideo.main.media
  (:require 
   [clojure.string :as s]
   [clojure.walk :as walk]
   [taoensso.timbre :as log]
   ["normalize-path" :as normalize-path]
   ["fast-glob" :as fast-glob]
   ["fluent-ffmpeg" :as ffmpeg-command]
   ["electron" :as electron :refer [ipcMain]]
   ["path" :as path]
   ["url" :as url]
   ["md5-file" :as md5]))

;;;;
;; general media functions

(defonce ffprobe-static-electron (js/require "ffprobe-static-electron"))
(defonce ffmpeg-static-electron (js/require "ffmpeg-static-electron"))

;; https://github.com/fluent-ffmpeg/node-fluent-ffmpeg#ffmpeg-and-ffprobe
(.setFfprobePath ffmpeg-command (.-path ffprobe-static-electron))
(.setFfmpegPath ffmpeg-command (.-path ffmpeg-static-electron))

(defn checksum [{:strs [filename] :as video}]
  (assoc video "md5sum" (.sync md5 filename)))

(defn db-info [video]
  ;; TODO enhance with db info
  (merge video {"info" {:a "changeme"}}))

(defn filter-keys [video]
  (-> video
    (select-keys ["filename" "size" "duration" "info" "md5sum"])
    (walk/keywordize-keys)))

(defn read-metadata [path]
  (js/Promise. 
   (fn [resolve reject]
     (.ffprobe 
      ffmpeg-command
      path 
      (fn [err metadata]
        (if err (reject err) (resolve (aget metadata "format"))))))))

(defn read-dir [dir]
  ;; TODO check if directory exists
  (let [extensions #{".mp4" ".avi"}
        patterns   (map #(str dir "/**/*" %) extensions)
        normalized (map normalize-path patterns)
        result     (fast-glob (clj->js normalized))]
    (log/debug "Reading directory with patterns" normalized)
    (-> result
        (.then #(do (log/infof "Read files %s" %) %))
        (.then #(js/Promise.all (map read-metadata %)))
        (.then #(js->clj %))
        (.then #(map checksum %))
        (.then #(map db-info %))
        (.then #(map filter-keys %))
        (.then #(sort-by :filename %)))))

;(.ffprobe ffmpeg-command "/Users/guidaveiga/Documents/Pictures/VID_20150606_172227117.mp4"
;  (fn [err metadata] (js/console.log err metadata)))
