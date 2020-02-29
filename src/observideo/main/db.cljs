(ns observideo.main.db
  (:require
   [taoensso.timbre :as log]
   [observideo.main.media :as media]
   ["electron" :as electron :refer [ipcMain app BrowserWindow crashReporter]]
   ["path" :as path]
   ["os" :as os]
   ["url" :as url]))


(def electron (js/require "electron"))
(def app  (.-app electron))
(def browser-window (.-BrowserWindow electron))
(def is-development? (boolean (or (.-defaultApp js/process)
                                (re-matches #"[\\/]electron-prebuilt[\\/]" (.-execPath js/process))
                                (re-matches #"[\\/]electron[\\/]" (.-execPath js/process)))))



(defn init []
  (let [db-path     (.getPath app "userData")
        sample-path (.getPath app "downloads")]
    #_
    (-> (media/read-dir sample-path)
        (.then #(ipc/send-message @contents :main/update-videos {:videos % :folder sample-path})))))
