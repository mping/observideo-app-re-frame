(ns observideo.renderer.components.videos
  (:require [cljs.core.async :as async :refer [go <! put!]]
            [clojure.string :as s]
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
        dir  (.showOpenDialog dialog opts)
        prm  (js/Promise.resolve dir)]                      ;;sometimes dir is a promise, sometimes it is not :\
    (.then prm (fn [arg]
                 (let [fps (aget arg "filePaths")
                       [dir] fps]
                   ;; TODO then? catch exceptions when user cancels?
                   (ipcrenderer/send-message :ui/update-videos-folder {:folder dir})
                   #(rf/dispatch [:ui/update-videos-folder {:folder dir}]))))))

;;;;
;; UI

(defn render-filename [text record] (r/as-element [:span (fname text)]))
(defn render-size [text record] (r/as-element [:span text]))
(defn render-duration [text record] (r/as-element [:span (int text) "s"]))
(defn render-info [val record]
  (let [info (js->clj val :keywordize-keys true)]
    (r/as-element [:span (:a info)])))

(defn render-actions [_ record]
  (r/as-element [antd/button {:type "primary" :size "small"
                              :onClick #(rf/dispatch [:ui/select-video (js->clj record :keywordize-keys true)])}
                 [antd/edit-icon]
                 " edit"]))

;;main listing
(defn videos-table []
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

(defn videos-list []
  [:div
   [:p]
   [antd/button {:type "primary" :onClick #(select-dir)} 
    [antd/upload-icon]
    " Open a directory"]
   [videos-table]])

;; main editing
(defn video-edit []
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

(defn show-video-panel [current]
  (if (some? current)
    video-edit
    videos-list))

(defn ui []
  (let [current @(rf/subscribe [:videos/current])]
    [(show-video-panel current)]))

;;;;
;; components

(comment

  (defsc VideoEdit [this {:folder/keys [selected] :as props} computed]
    {:query [:folder/selected]
     :ident (fn [] [:component/id ::video-edit])}
    (when selected
      (let [{:keys [filename duration]} selected]
        (log/info props)
        ; { tags:
        ;    { major_brand: 'mp42',
        ;      minor_version: '0',
        ;      compatible_brands: 'isommp42',
        ;      creation_time: '2018-08-20 18:07:46' },
        ;   probe_score: 100,
        ;   start_time: 0,
        ;   format_long_name: 'QuickTime / MOV',
        ;   duration: 11.516667,
        ;   size: 7690553,
        ;   filename:
        ;    '/Users/guidaveiga/Documents/Pictures/VID_20180820_190746.mp4',
        ;   nb_programs: 0,
        ;   nb_streams: 2,
        ;   bit_rate: 5342207,
        ;   format_name: 'mov,mp4,m4a,3gp,3g2,mj2'
        ;   info: {map}
        (dom/li
          (dom/h5 (fname filename))
          (dom/h5 (int duration) "s")))))

  (def ui-video-edit (comp/computed-factory VideoEdit {:keyfn :filename})))

