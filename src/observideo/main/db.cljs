(ns observideo.main.db
  (:require
   [taoensso.timbre :as log]
   [cognitect.transit :as t]
   [clojure.spec.alpha :as s]
   [observideo.common.datamodel :as datamodel]
   [goog.functions]
   ["electron" :as electron :refer [ipcMain app BrowserWindow crashReporter]]
   ["path" :as path]
   ["fs" :as fs]
   ["os" :as os]
   ["url" :as url])
  (:import [goog.async Debouncer]))


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

(defn- debounce [f interval]
  (let [dbnc (Debouncer. f interval)]
    ;; We use apply here to support functions of various arities
    (fn [& args] (.apply (.-fire dbnc) dbnc (to-array args)))))

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
        wrapped (wrap data)
        valid?  (s/valid? datamodel/db-spec data)]
    (when-not valid?
      (log/warn "Updating with invalid data")
      (s/explain datamodel/db-spec data))
    ;; lame, should be async
    (fs/writeFileSync db-file (t/write writer wrapped))))

;; debounced version
(def overwrite (goog.functions.debounce overwrite* 500))

(defn read [k]
  (get @db k))

(defn init []
  (log/infof "Initializing")
  (let [saved-db (read-db)]
    (reset! db saved-db)))
