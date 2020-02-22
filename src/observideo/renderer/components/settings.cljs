(ns observideo.renderer.components.settings
  (:require [cljs.core.async :as async :refer [go <! put!]]
            [goog.object :as gobj]
            [observideo.renderer.components.antd :as antd]))

(defn ui
  []
  [:div
   [:h1 "Settings"]])


