(ns observideo.renderer.ipcrenderer
  (:require [cljs.reader]
            [re-frame.core :as rf]
            [goog.object :as gobj]
            [taoensso.timbre :as log]
            [observideo.common.serde :as serde]))

;;;;;;;;;;;;;;;;;;;;;;;;
;;; IPC Main <> Renderer

(defonce electron (js/require "electron"))
(def ipcRenderer (gobj/get electron "ipcRenderer"))

;;;;
;; renderer >> main

;; post messages from renderer to main
(defn send-message [event data]
  (log/infof "[rend]-> [%s] %s" event data)
  (.send ipcRenderer "event" (serde/serialize {:event (subs (str event) 1) :data data})))
  ;(.send ipcRenderer "event" (clj->js {:event (subs (str event) 1) :data data})))

;; called when the renderer received an ipc message
(defmulti handle (fn [event _ _] event) :default :unknown)

(defmethod handle :main/update-videos [event sender data]
  (let [videos (:videos data)
        folder (:folder data)]
    (rf/dispatch [:main/update-videos {:videos videos :folder folder}])))

(defmethod handle :main/reset-db [event sender data]
  (rf/dispatch [:db/reset data]))

(defmethod handle :unknown [event sender data]
  (js/console.log "UNKNOWN" event sender data))

;; main handler
(defn handle-message [evt jsdata]
  (let [sender      (.-sender evt)
        datum (serde/deserialize jsdata)
        ;datum (js->clj jsdata :keywordize-keys true)
        {:keys [event data]} datum]
    (log/infof "[rend]<- [%s] %s" event data)
    (handle (keyword event) sender data)))
