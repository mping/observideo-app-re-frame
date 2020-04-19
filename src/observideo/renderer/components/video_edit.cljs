(ns observideo.renderer.components.video-edit
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

(defn- select-template [video id]
  (rf/dispatch [:ui/update-current-video-template (str id)]))

(defn- template-form []
  (let [template     @(rf/subscribe [:videos/current-template])
        attributes   (:attributes template)
        sorted-attrs (sort-by (fn [[_ v]] (:index v)) attributes)]
    [:div.ant-table.ant-table-middle
     [:div.ant-table-container
      [:div.ant-table-content
       ;; dynamic fields
       [:table {:style {:width "100%"}}
        [:thead.ant-table-thead
         ;; col header
         [:tr nil
          (concat (map-indexed (fn [i [header v]]
                                 [:th.ant-table-cell {:key (:index v)} (name header)])
                    sorted-attrs))]]

        [:tbody.ant-table-tbody
         [:tr nil
          ;; attrs list per header
          (for [[header v] sorted-attrs
                :let [vals  (:values v)
                      pairs (sort-by last (zipmap vals (range)))]]
            [:td {:style {:padding 0}
                  :key (str "attr" (:index v))
                  :valign "top"}
             [:table nil
              [:tbody nil
               ;; build an indexed [val, index]
               (for [pair pairs
                     :let [[val i] pair]]
                 [:tr {:key (str "row-" i)}
                  [:td {:key (str "cell-" i)} val]])]]])]]]]]]))

(defn root []
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
      (let [section           @(rf/subscribe [:videos/current-section])
            selected-template @(rf/subscribe [:videos/current-template])
            video-steps       (inc (+ (int (/ duration @!step-interval))
                                     (if (> 0 (mod duration @!step-interval))
                                       1
                                       0)))]

        (reset! !step-interval (get selected-template :interval 1))

        [:div
         [antd/row {:gutter [8, 8]}
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
                                                      (let [secs  (.-currentTime jsobj)
                                                            index (int (/ secs @!step-interval))]
                                                        (reset! video-time secs)
                                                        (reset! video-section index)
                                                        (rf/dispatch [:ui/update-current-video-section secs index]))))
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
           [template-form]]]
         [:hr]
         [antd/row
          [:h1 "Here:" (:time section) "|" (:index section)]]]))))

