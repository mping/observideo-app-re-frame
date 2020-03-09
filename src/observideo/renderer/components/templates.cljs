(ns observideo.renderer.components.templates
  (:require [goog.object :as gobj]
            [observideo.renderer.components.antd :as antd]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as str]))

(defn- add-template-col [template])

(defn- delete-template-col [template colindex])

(defn- delete-template-attr [e template name idx]
  (.preventDefault e)
  (let [attrs        (:attributes template)
        vals         (get attrs name)
        spliced      (vec (concat (subvec vals 0 idx) (subvec vals (inc idx))))
        new-template (assoc-in template [:attributes name] spliced)]
    (js/console.log template name idx)
    (js/console.log new-template)
    (rf/dispatch [:ui/update-template new-template])))

(defn- template-form []
  (let [template        @(rf/subscribe [:templates/current])
        {:keys [name attributes]} template
        headers         (keys attributes)
        dims-per-header attributes]
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
        ;; col header
        [:tr nil
         (concat (map (fn [item]
                        [:td {:key item}
                         [antd/input {:value item}]]) headers)
           [[:td {:key "add"}
             [antd/button {:type "link"} [antd/plus-circle-icon]]]])]]

       [:tbody nil
        [:tr nil
         ;; attrs list per header
         (for [header headers
               :let [vals (get dims-per-header header)]]
           [:td {:key header :valign "top"}
            [:table nil
             [:tbody nil
              ;; build an indexed [val, index]
              (for [pair (zipmap vals (range))
                    :let [[val i] pair]]
                [:tr {:key val}
                 [:td nil
                  [antd/input {:value      val
                               :size       "small"
                               :addonAfter (r/as-element
                                             [antd/button {:size "small"
                                                           :type "link"
                                                           :href "#"
                                                           :onClick #(delete-template-attr % template header i)}
                                              [antd/minus-circle-icon]])}]]])
              [:td nil
               [:td nil
                [antd/button {:size "small"}
                 [antd/plus-icon]]]]]]])]]]

      ;; save button
      [antd/form-item {}
       [antd/button {:type "primary" :htmlType "submit"}
        [antd/save-icon] " save"]]]]))

(defn- render-attributes [_ record]
  (let [clj-record (js->clj record :keywordize-keys true)]
    (str/join ", " (map name (keys (:attributes clj-record))))))

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
    [antd/table {:dataSource (vals templates)
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


