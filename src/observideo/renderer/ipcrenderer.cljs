(ns observideo.renderer.ipcrenderer
  (:require [cljs.reader]
            [re-frame.core :as rf]
            [goog.object :as gobj]
            [taoensso.timbre :as log]))


;; -- Domino 1 - Event Dispatch -----------------------------------------------

(defn dispatch-timer-event
  []
  (let [now (js/Date.)]
    (rf/dispatch [:timer now])))  ;; <-- dispatch used

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
  (.send ipcRenderer "event" (clj->js {:event event :data data})))

;; called when the renderer received an ipc message
(defn handle-message [_ js-data]
  (let [{:keys [event data]} (js->clj js-data :keywordize-keys true)]
    (log/info event data)

    (cond (= "main->update-videos" event)
          (let [videos (:videos data)
                folder (:folder data)]
            (rf/dispatch [:main/update-videos {:videos videos :folder folder}]))

          :else
          (log/warn "Unknown event" event data))))

