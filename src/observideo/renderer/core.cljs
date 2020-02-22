(ns observideo.renderer.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [goog.object :as gobj]
            ["nedb" :as nedb]
            [devtools.core :as devtools]
            [observideo.renderer.views :refer [ui]]
            [observideo.renderer.subs]
            [observideo.renderer.events]
            [observideo.renderer.ipcrenderer :as ipc]))


(devtools/install!)       ;; we love https://github.com/binaryage/cljs-devtools
(enable-console-print!)

(defonce electron (js/require "electron"))
(def ipcRenderer (gobj/get electron "ipcRenderer"))

;; -- Entry Point -------------------------------------------------------------

(defn ^:export init
  []
  (rf/dispatch-sync [:initialize])
  (.on ipcRenderer "event" ipc/handle-message)
  (reagent/render [observideo.renderer.views/ui]
                  (js/document.getElementById "app-container")))

(init)
