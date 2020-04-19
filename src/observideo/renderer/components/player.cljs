(ns observideo.renderer.components.player
  (:require [reagent.core :as reagent]
            ["video-react" :as VideoPlayer]))

(def video-player (reagent/adapt-react-class (.-Player VideoPlayer)))
