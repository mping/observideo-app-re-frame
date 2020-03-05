(ns observideo.main.ipcmain
  (:require
   [observideo.main.media :as media]
   [observideo.main.db :as db]
   [taoensso.timbre :as log]
   ["electron" :as electron :refer [BrowserWindow remote app ipcRender ipcMain]]
   ["path" :as path]
   ["url" :as url]))

;;;;
;; utils
;; https://github.com/brianium/tomaat/blob/master/src/tomaat/util.cljs
(def electron (js/require "electron"))
(def app (.-app electron))

(defn- browser-window-ctor []
  (or BrowserWindow (.-BrowserWindow remote)))

(defn current-window-id []
  (->> (browser-window-ctor)
    .getFocusedWindow
    .-id))

(defn- web-contents
  "Get the webContents of a browser window identified by id"
  [id]
  (->> id
    (.fromId (browser-window-ctor))
    .-webContents))


;; see https://github.com/electron/electron/blob/v3.0.16/docs/api/ipc-main.md

;;;;
;; IPC

(defn send-message
  ([event data]
   (send-message (web-contents (current-window-id)) event data))
  ([webcontents event data]
   (log/infof "[main]-> [%s] %s" event data)
   (.send webcontents "event" (clj->js {:event (subs (str event) 1) :data data}))))

;; called when the renderer received an ipc message
(defmulti handle (fn [event _ _] event) :default :unknown)

;;;;
;; ipc/ui
(defmethod handle :ui/update-videos-folder [_ sender data]
  (if-let [folder (:folder data)]
    ;; TODO loading event?
    (-> (media/read-dir folder)
        (.then #(send-message sender :main/update-videos {:videos % :folder folder})))
    (log/warnf ":ui/update-videos-folder called with empty folder: %s", data)))


(defmethod handle :ui/ready [_ sender _]
  (handle :ui/update-videos-folder sender {:folder (db/read :dir/videos)}))
;;;;
;; ipc/ui


(defmethod handle :unknown [event sender data]
  (js/console.log "UNKNOWN" event))

;;;;
;; main handler
(defn handle-message [evt jsdata]
  (let [sender (.-sender evt)
        datum  (js->clj jsdata :keywordize-keys true)
        {:keys [event data]} datum]
    (log/infof "[rend]<- [%s] %s" (keyword event) data)
    (handle (keyword event) sender data)))
