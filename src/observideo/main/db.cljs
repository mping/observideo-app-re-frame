(ns observideo.main.db
  (:require
   [taoensso.timbre :as log]
   [cognitect.transit :as t]
   ["electron" :as electron :refer [ipcMain app BrowserWindow crashReporter]]
   ["path" :as path]
   ["fs" :as fs]
   ["os" :as os]
   ["url" :as url]))


(def electron (js/require "electron"))
(def app (.-app electron))
(def browser-window (.-BrowserWindow electron))
(def is-development? (boolean (or (.-defaultApp js/process)
                                (re-matches #"[\\/]electron-prebuilt[\\/]" (.-execPath js/process))
                                (re-matches #"[\\/]electron[\\/]" (.-execPath js/process)))))

;; whole db
(def db-file (str (.getPath app "userData") "/observideo.db.transit"))
(def db (atom nil))

(defn read-db []
  (log/infof "Reading entire db at %s" db-file)
  (if (fs/existsSync db-file)
    (let [reader   (t/reader :json-verbose)
          ;; lame, should be async
          data     (fs/readFileSync db-file)
          clj-data (t/read reader data)]
      (reset! db clj-data))
    ;else
    (log/warnf "File does not exist: %s" db-file)))

(defn read [k]
  (get @db k))

(defn update-all [data]
  (log/infof "Updating entire db at %s" db-file)
  (let [writer (t/writer :json-verbose)]
    ;; lame, should be async
    (fs/writeFileSync db-file (t/write writer data))))

(defn init []
  (log/infof "Initializing")
  (let [saved-db (read-db)]
    (reset! db saved-db)))
