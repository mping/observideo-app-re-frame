(ns observideo.main.core
  (:require
   [taoensso.timbre :as log]
   [observideo.main.media :as media]
   [observideo.main.ipcmain :as ipc]
   ["electron" :as electron :refer [ipcMain app BrowserWindow crashReporter]]
   ["path" :as path]
   ["os" :as os]
   ["url" :as url]))


(def electron (js/require "electron"))
(def homedir (.homedir (js/require "os")))
(def dialog electron/dialog)
(def app  (.-app electron))
(def menu (.-Menu electron))
(def browser-window (.-BrowserWindow electron))
(def darwin? (= "darwin" js/process.platform))
(def is-development? (boolean (or (.-defaultApp js/process)
                                (re-matches #"[\\/]electron-prebuilt[\\/]" (.-execPath js/process))
                                (re-matches #"[\\/]electron[\\/]" (.-execPath js/process)))))

;;;;
;; global vars
(defonce main-window (atom nil))
(defonce contents (atom nil))

;;;;
;; commands

(defn- select-dir []
  (let [opts (clj->js {:properties ["openDirectory"]})
        dir  (.showOpenDialog dialog @main-window opts)]
    (ipc/send-message :open-directory dir)))


;;;;
;; menu

(defn- init-menu
  []
  (let [name     (.getName app)
        template (cond-> []
                   :always
                   (-> (concat [#_{:label   "File"
                                   :submenu [{:label       "Open Directory"
                                              :accelerator "CmdOrCtrl+O"
                                              :click       select-dir}]}]

                         (when is-development?
                           [{:label   "View"
                             :submenu [{:label       "Reload"
                                        :accelerator "CmdOrCtrl+R"
                                        :click       (fn [_ focusedWindow]
                                                       (when focusedWindow
                                                         (.reload focusedWindow)))}
                                       {:label       "Toggle Full Screen"
                                        :accelerator (if darwin? "Ctrl+Command+F" "F11")
                                        :click       (fn [_ focusedWindow]
                                                       (when focusedWindow
                                                         (let [full? (.isFullScreen focusedWindow)]
                                                           (.setFullScreen focusedWindow (not full?)))))}
                                       {:label       "Toggle Developer Tools"
                                        :accelerator (if darwin? "Alt+Command+I" "Ctrl+Shift+I")
                                        :click       (fn [_ focusedWindow]
                                                       (when focusedWindow
                                                         (.toggleDevTools focusedWindow)))}]}])
                         [{:label   "Window"
                           :role    "window"
                           :submenu [{:label       "Minimize"
                                      :accelerator "CmdOrCtrl+M"
                                      :role        "minimize"}
                                     {:label       "Close"
                                      :accelerator "CmdOrCtrl+W"
                                      :role        "close"}]}])
                     vec)

                   :always
                   clj->js)]
    (.setApplicationMenu menu (.buildFromTemplate menu template))))

(defn- init-db []
  (let [sample-path (str homedir "/Downloads")]
    (-> (media/read-dir sample-path)
      (.then #(ipc/send-message @contents :main->update-videos {:videos % :folder sample-path})))))


(defn- init-browser-window []
  (reset! main-window (browser-window.
                        (clj->js (merge {:width          800
                                         :height         600
                                         :webPreferences {:nodeIntegration true}
                                         :resizable      true}))))

  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on @main-window "closed" #(reset! main-window nil))
  (.on ipcMain "event" ipc/handle-message)
  (when is-development?
    (.. @main-window -webContents openDevTools))
  (reset! contents (.-webContents @main-window)))

(defn init []
  (init-menu)
  (init-db)
  (init-browser-window))

(defn init-browser []
  (reset! main-window (BrowserWindow.
                        (clj->js {:width 9000
                                  :height 600})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on @main-window "closed" #(reset! main-window nil)))

(defn main []
  ; CrashReporter can just be omitted
  #_
  (.start crashReporter
          (clj->js
            {:companyName "Observideo"
             :productName "ObservideoApp"
             :submitURL "https://observideo.com/submit-url"
             :autoSubmit false}))

  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init))
