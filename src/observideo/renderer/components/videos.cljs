(ns observideo.renderer.components.videos
  (:require [cljs.core.async :as async :refer [go <! put!]]
            [clojure.string :as s]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.object :as gobj]
            [taoensso.timbre :as log]
            [observideo.renderer.ipcrenderer :as ipcrenderer]
            [observideo.renderer.components.antd :as antd]))


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
                   ;; TODO then?
                   (ipcrenderer/send-message :update-videos-folder {:folder dir})
                   #(rf/dispatch [:main/update-videos-folder {:folder dir}]))))))

;;;;
;; UI

(defn render-filename [text record] (r/as-element [:span (fname text)]))
(defn render-size [text record] (r/as-element [:span text]))
(defn render-duration [text record] (r/as-element [:span (int text) "s"]))
(defn render-info [val record]
  (let [info (js->clj val :keywordize-keys true)]
    (r/as-element [:span (:a info)])))

(defn render-actions [_ record]
  (r/as-element [antd/button {:type "primary" :icon "edit" :size "small" :onClick #(js/console.log record)}
                 "edit"]))

(defn videos-list []
  (let [folder @(rf/subscribe [:videos/videos-folder])
        videos @(rf/subscribe [:videos/videos-list])]
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



(defn video-edit []
  [:h1 "Video"])

(defn ui
  []
  [:div
   [:h1 "Videos"]
   [:div
    [:p]
    [antd/button {:type "primary" :icon "upload" :onClick #(select-dir)} "Open a directory"]
    [videos-list]]])

(comment
  ;; function-as-a-child render fns
  (defn render-filename [text record] (dom/span (fname text)))
  (defn render-size [text record] (dom/span text))
  (defn render-duration [text record] (dom/span (int text) "s"))
  (defn render-info [val record]
    (let [info (js->clj val :keywordize-keys true)]
      (dom/span (:a info))))

  ;; action component, cannot use render function because we need the `this`
  (defsc Actions [this {:keys [text record]} {:keys [onClick]}]
    (antd/button {:type "primary" :icon "edit" :size "small" :onClick onClick}
      "edit"))
  (def ui-actions (comp/computed-factory Actions))

  (antd/table
    {:dataSource list}
    :rowKey :filename
    :size "small"
    :bordered true
    :pagination {:position "top"}
    :title #(str "current directory: " id)
    (antd/column {:title "File" :dataIndex :filename :key :filename :render render-filename})
    (antd/column {:title "Size" :dataIndex :size :key :size :render render-size})
    (antd/column {:title "Info" :dataIndex :info :key :info :render render-info})
    (antd/column {:title "Duration" :dataIndex :duration :key :duration :render render-duration})
    (antd/column
      {:title     "Actions"
       :dataIndex :action
       :key       :action
       :render    (fn [text record]
                    (comp/with-parent-context this
                      (ui-actions {:text text :record record}
                        {:onClick #(select-video this (js->clj record :keywordize-keys true))})))})))

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

