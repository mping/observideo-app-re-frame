(ns observideo.renderer.components.player
  (:require [reagent.core :as reagent]
            ["react-player" :as ReactPlayer :refer [default]]))

(def file-player (reagent/adapt-react-class default))

