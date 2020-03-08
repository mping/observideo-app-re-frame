(ns observideo.renderer.components.videos
  (:require [clojure.string :as s]
            [promesa.core :as p]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [observideo.renderer.ipcrenderer :as ipcrenderer]
            [observideo.renderer.components.antd :as antd]
            [observideo.renderer.components.player :as player]))


(defonce electron (js/require "electron"))
(defonce remote (.-remote electron))
(defonce dialog (.-dialog remote))

(defn fname [path]
  ;;TODO use os.separator
  (subs path (inc (s/last-index-of path "/"))))

;;;;
;; actions

(defn- select-dir []
  (let [opts (clj->js {:properties ["openDirectory"]})
        dir  (.showOpenDialog dialog opts)]
    (-> (p/resolved dir)
      (p/then (fn [arg]
                (let [[dir] (aget arg "filePaths")]
                  (rf/dispatch [:ui/update-videos-folder {:folder dir}]))))
      (p/catch (fn [err] (log/warn err))))))

;;;;
;; UI

(defn- render-filename [text record] (r/as-element [:span (fname text)]))
(defn- render-size [text record] (r/as-element [:span text]))
(defn- render-duration [text record] (r/as-element [:span (int text) "s"]))
(defn- render-info [val record]
  (let [info (js->clj val :keywordize-keys true)]
    (r/as-element [:span (:a info)])))

(defn- render-actions [_ record]
  (r/as-element [antd/button {:type    "primary" :size "small"
                              :onClick #(rf/dispatch [:ui/select-video (js->clj record :keywordize-keys true)])}
                 [antd/edit-icon]
                 " edit"]))

;;main listing
(defn- videos-table []
  (let [folder @(rf/subscribe [:videos/folder])
        videos @(rf/subscribe [:videos/list])]
    [antd/table {:dataSource videos
                 :rowKey     :filename
                 :size       "small"
                 :bordered   true
                 :pagination {:position "top"}
                 :title      #(str "current directory: " folder)}
     [antd/column {:title "File" :dataIndex :filename :key :filename :render render-filename}]
     [antd/column {:title "Size" :dataIndex :size :key :size :render render-size}]
     [antd/column {:title "Info" :dataIndex :info :key :info :render render-info}]
     [antd/column {:title "Duration" :dataIndex :duration :key :duration :render render-duration}]
     [antd/column {:title     "Actions"
                   :dataIndex :action
                   :key       :action
                   :render    render-actions}]]))

(defn- videos-list []
  [:div
   [:p]
   [antd/button {:type "primary" :onClick #(select-dir)}
    [antd/upload-icon]
    " Open a directory"]
   [videos-table]])

;; main editing
(defn- video-edit []
  (let [current  @(rf/subscribe [:videos/current])
        filename (:filename current)]
    [:div
     [antd/row
      [antd/col {:span 12}
       [antd/page-header {:title  (fname filename) :subTitle filename
                          :onBack #(rf/dispatch [:ui/deselect-video])}]
       [player/file-player {:url      (str "file://" (:filename current))
                            :controls true
                            :width    "100%"}]]
      [antd/col {:span 12}
       [antd/page-header {:title "Template"}]]]]))

(defn- show-video-panel [current]
  (if (some? current)
    video-edit
    videos-list))

(defn- breadcrumbs []
  (let [folder  @(rf/subscribe [:videos/folder])
        current @(rf/subscribe [:videos/current])
        vname   (:filename current)]
    [antd/breadcrumb
     [antd/breadcrumb-item "videos"]
     [antd/breadcrumb-item
      [:a {:onClick #(rf/dispatch [:ui/deselect-video])} folder]]
     (when vname
       [antd/breadcrumb-item (fname vname)])]))

(defn ui []
  (let [current @(rf/subscribe [:videos/current])]
    [:div
     [breadcrumbs]
     [(show-video-panel current)]]))
