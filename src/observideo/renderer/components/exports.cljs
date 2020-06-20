(ns observideo.renderer.components.exports
  (:require [clojure.string :as s]
            [promesa.core :as p]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [observideo.common.utils :as utils]
            [observideo.renderer.ipcrenderer :as ipcrenderer]
            [observideo.renderer.components.antd :as antd]))

(defn- start-export [opts]
  (rf/dispatch [:db/export opts]))

(defn ui []
  [:div
   [:h1 "Export data"]
   [:p "Download all your video observations as a csv file.
       A zip file with a csv per video will be created."]
   [:hr]
   [:div
    [antd/row {:gutter [8,8]}
     [antd/col {:span 12}
      [:div
       [antd/button {:type "primary" :onClick #(start-export {:by :index1})}
        [antd/download-icon]
        " Export to CSV (index starts at 1)"]
       [:p "Export the data as zip, with one CSV file per video. Indexed based."]
       [:p "Each csv file will have the data with the following format"]
       [:pre"Peer,Gender
1,1
1,2
 ,2
etc"]]]

     [antd/col {:span 12} 
      [:div
       [antd/button {:type "primary" :onClick #(start-export {:by :name})}
        [antd/download-icon]
        " Export to CSV (name)"]
       [:p "Export the data as CSV, name based."]
       [:p "Each csv file will have the data with the following format"]
       [:pre "Peer,Gender
Alone,Same
Adults,Both
etc"]]]]]])

