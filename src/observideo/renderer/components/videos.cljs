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


;;;;
;;main listing

(defn- render-filename [text record] (r/as-element [:span (fname text)]))
(defn- render-size [text record] (r/as-element [:span text]))
(defn- render-template [text record]
  (let [templates @(rf/subscribe [:templates/all])]
    (r/as-element [:span (get-in templates [text :name])])))
(defn- render-duration [text record] (r/as-element [:span (int text) "s"]))
(defn- render-info [val record]
  (let [info (js->clj val :keywordize-keys true)]
    (r/as-element [:span (:a info)])))

(defn- render-actions [_ record]
  (r/as-element [antd/button {:type    "primary" :size "small"
                              :onClick #(rf/dispatch [:ui/select-video (js->clj record :keywordize-keys true)])}
                 [antd/edit-icon]
                 " edit"]))

(defn- videos-table []
  (let [folder @(rf/subscribe [:videos/folder])
        videos @(rf/subscribe [:videos/all])
        videos (vals videos)]
    [antd/table {:dataSource videos
                 :rowKey     :filename
                 :size       "small"
                 :bordered   true
                 :pagination {:position "top"}
                 :title      #(str "current directory: " folder)}
     [antd/column {:title "File" :dataIndex :filename :key :filename :render render-filename}]
     [antd/column {:title "Size" :dataIndex :size :key :size :render render-size}]
     [antd/column {:title "Template" :dataIndex :template-id :key :template-id :render render-template}]
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

;;;;
;; main editing

(defn- select-template [video id]
  (rf/dispatch [:ui/update-current-video-template (str id)]))

(defn- move-video-time [seconds])

(defn- video-edit []
  (let [video          @(rf/subscribe [:videos/current])
        {:keys [duration filename]} video
        templates      (vals @(rf/subscribe [:templates/all]))

        ;; these are "local component state"
        ;; some can be used to re-trigger a render (r/atom)
        ;; others are just vars (clojure.core/atom)
        !video-player  (clojure.core/atom nil)
        !step-interval (clojure.core/atom 1)
        video-section  (r/atom 0)
        video-time     (r/atom 0)]

    ;; form-2 component
    (fn []
      ;; trigger re-render when some attr on the video changes
      (let [selected-template @(rf/subscribe [:videos/current-template])
            video-steps       (inc (+ (int (/ duration @!step-interval))
                                     (if (> 0 (mod duration @!step-interval))
                                       1
                                       0)))]

        (reset! !step-interval (get selected-template :interval 1))

        [:div
         [antd/row
          ;;;;
          ;; left col - video player
          [antd/col {:span 12}
           [antd/page-header {:title  (fname filename) :subTitle filename
                              :onBack #(rf/dispatch [:ui/deselect-video])}]
           [player/video-player {:playsInline true
                                 :src         (str "file://" (:filename video))
                                 :ref         (fn [el]
                                                (when (some? el)
                                                  (.subscribeToStateChange el
                                                    (fn [jsobj]
                                                      (let [secs (.-currentTime jsobj)]
                                                        (reset! video-time secs)
                                                        (reset! video-section (int (/ secs @!step-interval))))))
                                                  (reset! !video-player el)))}]]

          ;;;;
          ;; right col - template application

          [antd/col {:span 12}
           [antd/page-header {:title "Template"}]
           [:div
            [antd/select {:defaultValue (str (:id selected-template)) :onChange #(select-template video %)}
             (for [tmpl templates
                   :let [{:keys [id name]} tmpl]]
               [antd/option {:key id} name])]
            [:span (str "interval: " @!step-interval "s")]]
           [antd/slider {:min            0
                         :max            video-steps
                         :value          @video-section
                         :key            "video-section-slider"
                         :tooltipVisible true
                         :dots           true
                         :onChange       #(do (reset! video-section %)
                                              (.seek @!video-player (* % @!step-interval) "seconds")
                                              (.pause @!video-player))}]
           [:h3 "XXX"
            [:pre (js/JSON.stringify (clj->js selected-template) nil 1 2)]]]]
         [:hr]
         [antd/row
          [:h1 "Here:" @video-time "|" @video-section]]]))))


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
