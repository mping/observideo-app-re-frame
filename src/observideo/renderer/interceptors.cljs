(ns observideo.renderer.interceptors
  (:require [re-frame.core :as r]
            [taoensso.timbre :as log]
            [observideo.renderer.ipcrenderer :as ipcrenderer]))

;; sends the event as an IPC message
(def event->ipc
  (r/->interceptor
    :id :ipc2main
    :after (fn [{:keys [coeffects] :as context}]
             (let [[event data] (:event coeffects)]
               (ipcrenderer/send-message event data)
               context))))

;; Queues a DB save on the background
;; will always send whole DB
(def queue-save-db
  (r/->interceptor
    :id :queue-save
    :after (fn [{:keys [effects] :as context}]
             (let [db (:db effects)]
               (ipcrenderer/send-message :db/update db)
               (js/console.log "QUEUE save" db)
               context))))
