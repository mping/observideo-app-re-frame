(ns observideo.renderer.components.templates
  (:require [goog.object :as gobj]
            [observideo.renderer.components.antd :as antd]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as str]))

(defn- template-form []
  (let [template @(rf/subscribe [:templates/current])
        {:keys [name]} template]
    [:div
     [antd/page-header {:title  name :subTitle "Edit"
                        :onBack #(rf/dispatch [:ui/deselect-template])}]
     [:h1 "Template"]]))

(defn- render-features [_ record]
  (let [clj-record (js->clj record :keywordize-keys true)]
    (js/console.log (:dimensions clj-record))
    (str/join ", " (map :name (:dimensions clj-record)))))

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
     [antd/column {:title "Features" :dataIndex :name :render render-features}]
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
        name   (:name current)]
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


