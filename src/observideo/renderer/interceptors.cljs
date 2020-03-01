(ns observideo.renderer.interceptors
  (:require [re-frame.core :as r]
            [taoensso.timbre :as log]
            [observideo.renderer.ipcrenderer :as ipcrenderer]))


(def log-event
  (r/->interceptor
    :id :trim-event
    :after (fn [context]
             (log/warn "XAXAXA" context))))

    ;; :coeffects {:event [:ui/update-videos-folder {:folder "/home/mping/Downloads"}],
    ;; :db ...))


(def ipc2main-interceptor
  (r/->interceptor
    :id :ipc2main
    :after (fn [{:keys [coeffects] :as context}]
             (let [[event data] (:event coeffects)]
               (ipcrenderer/send-message event data)))))