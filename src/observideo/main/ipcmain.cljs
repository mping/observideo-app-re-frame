(ns observideo.main.ipcmain
  (:require 
   [observideo.main.media :as media]
   [taoensso.timbre :as log]
   ["electron" :as electron :refer [BrowserWindow remote app ipcRender ipcMain]]
   ["path" :as path]
   ["url" :as url]))

;;;;
;; utils
;; https://github.com/brianium/tomaat/blob/master/src/tomaat/util.cljs

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
   (log/info (str "[main] send " event) data)
   (.send webcontents "event" (clj->js {:event event :data data}))))


;; called when the renderer received an ipc message
(defn handle-message [evt js-data]
  (let [original-sender      (.-sender evt)
        {:keys [event data]} (js->clj js-data :keywordize-keys true)]
    (log/info "[main][" event "]" js-data)

    (cond (= "update-videos-folder" event)
          (let [folder (:folder data)]
            (js/console.log "update vids" folder)
            (-> (media/read-dir folder)
                (.then #(send-message original-sender :main->update-videos {:videos % :folder folder}))))

          :else
          (log/info "Unknow event" event ", type" (type event) ", data" data))))


;; TODO use app.getPath to store db
; https://github.com/electron/electron/blob/master/docs/api/app.md#appgetpathname
