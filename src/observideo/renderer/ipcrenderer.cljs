(ns observideo.renderer.ipcrenderer
  (:require [cljs.reader]
            [re-frame.core :as rf]
            [goog.object :as gobj]
            [taoensso.timbre :as log]))


;; -- Domino 1 - Event Dispatch -----------------------------------------------

(defn dispatch-timer-event
  []
  (let [now (js/Date.)]
    (rf/dispatch [:timer now])))                            ;; <-- dispatch used

;; Call the dispatching function every second.
;; `defonce` is like `def` but it ensures only one instance is ever
;; created in the face of figwheel hot-reloading of this file.
;; (defonce do-timer (js/setInterval dispatch-timer-event 1000))


;;;;;;;;;;;;;;;;;;;;;;;;
;;; IPC Main <> Renderer

(defonce electron (js/require "electron"))
(def ipcRenderer (gobj/get electron "ipcRenderer"))

;;;;
;; renderer >> main

;; post messages from renderer to main
(defn send-message [event data]
  (log/infof "Sending [%s] %s" event data)
  (.send ipcRenderer "event" (clj->js {:event (subs (str event) 1) :data data})))

;; called when the renderer received an ipc message
(defmulti handle (fn [_ event _] event) :default :unknown)

(defmethod handle :main/update-videos [channel event data]
  (let [videos (:videos data)
        folder (:folder data)]
    (rf/dispatch [:main/update-videos {:videos videos :folder folder}])))

(defmethod handle :unknown [channel event data]
  (js/console.log "UNKNOWN" channel event data))

;; main handler
(defn handle-message [channel jsdata]
  (let [datum (js->clj jsdata :keywordize-keys true)
        {:keys [event data]} datum]
    (log/infof "[%s] %s" event data datum)
    (handle channel (keyword event) data)))
