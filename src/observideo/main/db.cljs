(ns observideo.main.db
  (:require
   [taoensso.timbre :as log]
   ["electron" :as electron :refer [ipcMain app BrowserWindow crashReporter]]
   ["path" :as path]
   ["os" :as os]
   ["url" :as url]))


(def electron (js/require "electron"))
(def app (.-app electron))
(def browser-window (.-BrowserWindow electron))
(def is-development? (boolean (or (.-defaultApp js/process)
                                (re-matches #"[\\/]electron-prebuilt[\\/]" (.-execPath js/process))
                                (re-matches #"[\\/]electron[\\/]" (.-execPath js/process)))))
(def db (atom nil))

(defn read-db [path] nil)

(defn read [k]
  (get @db k))

(defn write [k v]
  (swap! db assoc k v))

(defn init []
  (let [app-dir  (.getPath app "userData")
        saved-db (read-db app-dir)]
    (reset! db (merge
                 {:dir/videos (.getPath app "downloads")}
                 saved-db))))
