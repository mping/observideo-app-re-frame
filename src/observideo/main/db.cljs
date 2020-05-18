(ns observideo.main.db
  (:require
    [taoensso.timbre :as log]
    [cognitect.transit :as t]
    [clojure.spec.alpha :as s]
    [observideo.common.datamodel :as datamodel]
    [goog.functions]
    ["electron" :as electron :refer [ipcMain app BrowserWindow crashReporter]]
    ["fs" :as fs]
    [promesa.core :as p]))

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
   :data (assoc datum :observideo/filename db-file)})

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
  ;;TODO debounce db updates
  (log/infof "Updating entire db at %s" db-file)
  (let [writer (t/writer :json-verbose)
        resetted (reset-view data)
        wrapped  (wrap resetted)
        valid?  (s/valid? datamodel/db-spec resetted)]
    (when-not valid?
      (log/warn "Updating with invalid data")
      (s/explain datamodel/db-spec resetted))
    ;; lame, should be async
    (fs/writeFileSync db-file (t/write writer wrapped))))

;; debounced version
(def overwrite (goog.functions.debounce overwrite* 500))

(defn read [k]
  (get @db k))

(defn export-to-csv []
  ;; export db to csv, returning a file path
  (p/resolved "file:///home/mping/Devel/workspace/observideo/package.json"))

(defn init []
  (log/infof "Initializing")
  (let [saved-db (read-db)]
    (reset! db saved-db)))
