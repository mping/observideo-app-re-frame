(ns observideo.main.media
  (:require 
   [clojure.walk :as walk]
   [taoensso.timbre :as log]
   ["path" :as path]
   ["normalize-path" :as normalize-path]
   ["fast-glob" :as fast-glob]
   ["fluent-ffmpeg" :as ffmpeg-command]
   ["md5-file" :as md5]))

;;;;
;; general media functions

(defonce ffprobe-static-electron (js/require "ffprobe-static-electron"))
(defonce ffmpeg-static-electron (js/require "ffmpeg-static-electron"))

;; https://github.com/fluent-ffmpeg/node-fluent-ffmpeg#ffmpeg-and-ffprobe
;(.setFfprobePath ffmpeg-command (normalize-path (.-path ffprobe-static-electron)))
;(.setFfmpegPath ffmpeg-command (normalize-path  (.-path ffmpeg-static-electron)))

;; https://github.com/joshwnj/ffprobe-static/issues/5

(if (not (.includes (js* "__dirname") ".asar"))
		  (do
					
					(log/debug "Using default ffprobe/ffmpeg paths:"  ffmpath)
					(log/debug "ffmpeg:" (.-path ffmpeg-static-electron))
					(log/debug "ffprobe:"  (.-path ffmpeg-static-electron))
		  	(.setFfmpegPath  ffmpeg-command (.-path ffmpeg-static-electron))
		  	(.setFfprobePath ffmpeg-command (.-path ffmpeg-static-electron)))

		  (let [ext     (if (= (.-platform js/process) "win32") ".exe" "")
		        ffppath (.join path (str (.-resourcesPath js/process) "/ffprobe" ext))
		        ffmpath (.join path (str (.-resourcesPath js/process) "/ffmpeg"  ext))]
			
			   (log/debug "Using resources path:")
						(log/debug "ffmpeg:"  ffmpath)
						(log/debug "ffprobe:" ffppath)

	     (.setFfmpegPath ffmpeg-command  (normalize-path ffppath))
		  	 (.setFfprobePath ffmpeg-command (normalize-path ffmpath))))

(defn checksum [{:strs [filename] :as video}]
  (assoc video "md5sum" (.sync md5 filename)))

(defn db-info [video]
  (merge video {"missing" false
                "info" {:a "changeme"}}))

(defn filter-keys [video]
  (-> video
    (select-keys ["filename" "size" "duration" "info" "md5sum" "missing"])
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
  (let [extensions #{".mp4" ".avi" ".webm"}
        patterns   (map #(str dir "/**/*" %) extensions)
        normalized (map normalize-path patterns)
        result     (fast-glob (clj->js normalized) #js {:caseSensitiveMatch false})]
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
