(ns observideo.renderer.components.templates
  (:require [goog.object :as gobj]
            [observideo.renderer.components.antd :as antd]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as str]))

;;;;
;; form evt handlers
(defn- add-template-col [e template]
  (.preventDefault e)
  (let [attrname     (str (gensym))
        new-template (update-in template [:attributes] assoc attrname ["Some" "Values"])]
    (rf/dispatch [:ui/update-template new-template])))

(defn- delete-template-col [e template name]
  (.preventDefault e)
  (let [new-template  (update-in template [:attributes] dissoc name)]
    (rf/dispatch [:ui/update-template new-template])))

(defn- update-template-col [template name newname]
  (let [new-template  (update-in template [:attributes] clojure.set/rename-keys {name newname})]
    (rf/dispatch [:ui/update-template new-template])))

(defn- add-template-attr [e template header]
  (.preventDefault e)
  (let [current-attrs (get-in template [:attributes header])
        new-attrs     (vec (conj current-attrs (str (gensym))))]
    (let [new-template (assoc-in template [:attributes header] new-attrs)]
      (rf/dispatch [:ui/update-template new-template]))))

(defn- delete-template-attr [e template name idx]
  (.preventDefault e)
  (let [attrs        (:attributes template)
        vals         (get attrs name)
        spliced      (vec (concat (subvec vals 0 idx) (subvec vals (inc idx))))
        new-template (assoc-in template [:attributes name] spliced)]
    (rf/dispatch [:ui/update-template new-template])))

;;;;
;; form

(defn- template-form []
  (let [template        @(rf/subscribe [:templates/current])
        {:keys [name attributes]} template
        headers         (keys attributes)
        dims-per-header attributes]
    [:div
     [antd/page-header {:title  name :subTitle "Edit"
                        :onBack #(rf/dispatch [:ui/deselect-template])}]
     [antd/form {:labelCol   {:span 6}
                 :wrapperCol {:span 14}
                 :name       "basic"
                 :size       "small"
                 #_#_:onFinish #(js/console.log %)}
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
         (concat (map (fn [header]
                        [:td {:key header}
                         [antd/input {:value      header
                                      :onChange   #(update-template-col template header (-> % .-target .-value))
                                      :size       "small"
                                      :addonAfter (r/as-element
                                                    [antd/button {:size    "small"
                                                                  :type    "link"
                                                                  :href    "#"
                                                                  :onClick #(delete-template-col % template header)}
                                                     [antd/delete-icon]])}]])
                   headers)
           [[:td {:key "add"}
             ;; add a new column
             [antd/button {:type "link" :onClick #(add-template-col % template)}
              [antd/plus-circle-icon]]]])]]

       [:tbody nil
        [:tr nil
         ;; attrs list per header
         (for [header headers
               :let [vals  (get dims-per-header header)
                     pairs (sort-by last (zipmap vals (range)))]]
           [:td {:key header :valign "top"}
            [:table nil
             [:tbody nil
              ;; build an indexed [val, index]
              (for [pair pairs
                    :let [[val i] pair]]
                [:tr {:key (str "row-" i)}
                 [:td {:key (str "cell-" i)}
                  [antd/input {:value      val
                               :key        i
                               :size       "small"
                               :addonAfter (r/as-element
                                             [antd/button {:size    "small"
                                                           :type    "link"
                                                           :href    "#"
                                                           :onClick #(delete-template-attr % template header i)}
                                              [antd/minus-circle-icon]])}]]])
              [:tr nil
               [:td nil
                ;; add a new row
                [antd/button {:size "small" :type "link" :onClick #(add-template-attr % template header)}
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
                 :rowKey     "name"
                 :bordered   true
                 :pagination {:position "top"}
                 :title      (constantly "Templates")}
     [antd/column {:title "Name" :dataIndex :name}]
     [antd/column {:title "Attributes" :render render-attributes}]
     [antd/column {:title "Actions" :render render-actions}]]))


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


