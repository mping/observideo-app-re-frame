(ns observideo.renderer.components.templates
  (:require [goog.object :as gobj]
            [observideo.renderer.components.antd :as antd]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as str]))

(defn- template-form []
  (let [template        @(rf/subscribe [:templates/current])
        {:keys [name attributes]} template
        headers         (map :name attributes)
        dims-per-header (reduce (fn [acc item] (assoc acc (:name item) (:values item))) {} attributes)]
    (js/console.log headers)
    (js/console.log dims-per-header)

    [:div
     [antd/page-header {:title  name :subTitle "Edit"
                        :onBack #(rf/dispatch [:ui/deselect-template])}]
     [antd/form {:labelCol   {:span 6}
                 :wrapperCol {:span 14}
                 :name       "basic"
                 :size       "small"
                 :onFinish   #(js/console.log %)}
      ;; main name
      [antd/form-item {:label "Template Name" :name "name" :rules [{:required true :message "Field is required"}]}
       [antd/input]]

      [antd/form-item {:label "Interval (secs)" :name "interval" :rules [{:required true :message "Field is required"}]}
       [antd/slider {:min 1 :max 60 :defaultValue 15 :tooltipVisible true}]]

      ;; dynamic fields
      [:table {:style {:width "100%"}}
       [:thead nil
        [:tr nil
         (map (fn [item] [:td item])
              (concat headers [antd/plus-circle-icon]))]]
       [:tbody nil
        [:tr nil
         (for [header headers
               :let [vals (get dims-per-header header)]]
           [:td {:key header :valign "top"}
            [:ul nil
             (for [val vals]
               [:li {:key val}
                [antd/input {:value val :addonAfter (r/as-element [antd/button {:size "small"} [antd/minus-circle-icon]])}]])
             [:li
              [antd/button {}
               [antd/plus-circle-icon]]]]])]]]

      ;; save button
      [antd/form-item {}
       [antd/button {:type "primary" :htmlType "submit"}
        [antd/save-icon] " save"]]]]))

(defn- render-attributes [_ record]
  (let [clj-record (js->clj record :keywordize-keys true)]
    (js/console.log (:attributes clj-record))
    (str/join ", " (map :name (:attributes clj-record)))))

(defn- render-actions [_ record]
  (r/as-element
    [:div
     [antd/button {:type    "primary" :size "small"
                   :onClick #(rf/dispatch [:ui/edit-template (js->clj record :keywordize-keys true)])}
      [antd/edit-icon] " edit"]
     [antd/button {:type    "danger" :size "small"
                   :onClick #(rf/dispatch [:ui/delete-template (js->clj record :keywordize-keys true)])}
      [antd/delete-icon] " delete"]]))

(defn- templates-table []
  (let [templates @(rf/subscribe [:templates/list])]
    [antd/table {:dataSource templates
                 :size       "small"
                 :rowKey     :name
                 :bordered   true
                 :pagination {:position "top"}
                 :title      (constantly "Templates")}
     [antd/column {:title "Name" :dataIndex :name :key :name}]
     [antd/column {:title "Attributes" :dataIndex :name :render render-attributes}]
     [antd/column {:title     "Actions"
                   :dataIndex :action
                   :key       :action
                   :render    render-actions}]]))


(defn show-template-panel [current]
  (if (some? current)
    template-form
    templates-table))

(defn- breadcrumbs []
  (let [current @(rf/subscribe [:templates/current])
        name    (:name current)]
    [antd/breadcrumb
     [antd/breadcrumb-item
      [:a {:onClick #(rf/dispatch [:ui/deselect-template])} "templates"]]
     (when name
       [antd/breadcrumb-item name])]))


(defn ui []
  (let [current @(rf/subscribe [:templates/current])]
    [:div
     [breadcrumbs]
     [(show-template-panel current)]]))


