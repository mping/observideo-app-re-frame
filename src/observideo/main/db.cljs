(ns observideo.main.db
  (:require
   [taoensso.timbre :as log]
   [cognitect.transit :as t]
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [observideo.common.datamodel :as datamodel]
   [goog.functions]
   [promesa.core :as p]
   ["electron" :as electron :refer [ipcMain app BrowserWindow crashReporter]]
   ["fs" :as fs]
   ["jszip" :as jszip]
   ["normalize-path" :as normalize-path]))

(def electron (js/require "electron"))
(def app (.-app electron))
(def browser-window (.-BrowserWindow electron))
(def is-development? (boolean (or (.-defaultApp js/process)
                                (re-matches #"[\\/]electron-prebuilt[\\/]" (.-execPath js/process))
                                (re-matches #"[\\/]electron[\\/]" (.-execPath js/process)))))

;; whole db
(def db-file (str (.getPath app "userData") "/observideo.db.transit"))
(def db (atom nil))

;; add/remove envelope
;; may be helpful in the future to facilitate data migrations
(defn- wrap [datum]
  {:version 1
   :data    (assoc datum :observideo/filename db-file)})

(defn- unwrap [wrapped]
  (get wrapped :data))

(defn- reset-view [data]
  (-> data
    (assoc :ui/tab :videos
           :videos/current nil
           :templates/current nil)))

(defn- read-db []
  (log/infof "Reading entire db at %s" db-file)
  (if (fs/existsSync db-file)
    (let [reader    (t/reader :json-verbose)
          ;; lame, should be async
          data      (fs/readFileSync db-file)
          clj-data  (t/read reader data)
          unwrapped (unwrap clj-data)]
      (reset! db unwrapped))
    ;else
    (log/warnf "File does not exist: %s" db-file)))

(defn- overwrite* [data]
  (log/infof "Updating entire db at %s" db-file)
  (let [writer   (t/writer :json-verbose)
        resetted (reset-view data)
        wrapped  (wrap resetted)
        valid?   (s/valid? datamodel/db-spec resetted)]
    (when-not valid?
      (log/warn "Updating with invalid data")
      (s/explain datamodel/db-spec resetted))
    ;; lame, should be async
    (let [res (fs/writeFileSync db-file (t/write writer wrapped))]
      (log/infof "Finished updating db at %s" db-file)
      res)))


;; debounced version
(def overwrite (goog.functions.debounce overwrite* 500))

(defn read [k]
  (get @db k))

;;;;
;; export ops

(defn filename->tmpfile
  "Generates a csv file based on the video filename"
  [basedir filename]
  (let [orig (string/replace-first filename basedir "")
        orig (string/replace-all orig #"[\\/]" "_")]
    (str orig ".csv")))

(defn- export-to-csv*
  "Exports a zip with one file per video.
  Returns a promise of a file with all exported data."
  [db {:keys [by] :as opts}]
  (let [archive (normalize-path (str (.getPath app "temp") "/observideo-export.csv.zip"))
        files   (datamodel/db->csv db)
        zip     (jszip.)]

    ;; write all files, then archive
    (log/infof "Going to export %s videos" (count files))
    (doseq [file files
            :let [{:keys [filename by-name by-index0 by-index1]} file
                  datum    (cond (= by :name) by-name
                                 (= by :index1) by-index1
                                 (= by :index0) by-index0)

                  ;; join cells, then lines
                  csvdatum (map #(string/join "," %) datum)
                  csvdatum (string/join "\n" csvdatum)

                  basedir  (:videos/folder db)
                  tmpfile  (filename->tmpfile basedir filename)]]

      ;; accumulate the file on the zip object
      (.file zip tmpfile csvdatum))

    (p/then
      (.generateAsync zip #js {:type        "nodebuffer"
                               :platform    js/process.platform
                               :compression "DEFLATE"})
      ;; TODO convert to something nicer?
      (fn [buff]
        (p/create
          (fn [resolve reject]
            (fs/writeFile archive buff
              (fn [err]
                (log/infof "Exported archive %s, err? %s" archive err)
                (if err
                  (reject err)
                  (resolve (str "file://" archive)))))))))))


(comment
  (p/then (export-to-csv* (read-db) {:by :name})
    #(println "resolved" %)))

(defn export-to-csv
  "Handles export requests"
  [{:keys [by] :as opts}]
  (if (nil? (#{:name :index1 :index0} by))
    (do
      (log/errorf "Unknown export format: '%s'" by)
      (p/rejected ("Unknown export format")))
    (export-to-csv* (read-db) opts)))

;;;;
;; main
(defn init []
  (log/infof "Initializing")
  (let [saved-db (read-db)]
    (reset! db saved-db)))
