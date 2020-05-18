(ns observideo.renderer.components.exports
  (:require [clojure.string :as s]
            [promesa.core :as p]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [observideo.common.utils :as utils]
            [observideo.renderer.ipcrenderer :as ipcrenderer]
            [observideo.renderer.components.antd :as antd]))

(defn- start-export []
  (rf/dispatch [:db/export]))

(defn ui []
  [:div
   [:h1 "Export data"]
   [:p "Click the button to download all your video observations as a csv file."]
   [:div
    [antd/button {:type "primary" :onClick #(start-export)}
     [antd/download-icon]
     " Export to CSV"]]])

