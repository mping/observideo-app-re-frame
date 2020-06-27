(ns observideo.renderer.components.queries
  (:require [observideo.renderer.components.antd :as antd]
            [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [reagent.core :as r]))

(defn- select-template-values
  "Draws a row of [select1] [...] [selectN] dropdowns,
  one for each template attribute."
  [{:keys [attributes] :as template}
   {:keys [on-change]}]
  [:table
   [:tbody
    [:tr
     (for [[k v] attributes
           :let [vals (:values v)]]
       [:td
        [:b k]
        [antd/select {:onChange #(on-change k %) :allowClear true}
         (for [opt vals]
           [antd/option {:key opt} opt])]])]]])

(defn ui
  []
  (let [templates-map     @(rf/subscribe [:templates/all])
        templates         (vals templates-map)
        ;; local state
        current-template! (r/atom nil)
        on-change-num (fn [k v] (log/info "num" k v))
        on-change-den (fn [k v] (log/info "den" k v))]
    (fn []
      [:div
       [:h1 "Queries"]
       ;; select a component
       [antd/select {:onChange     #(reset! current-template! (get templates-map %))}
        (for [tmpl templates
              :let [{:keys [id name]} tmpl]]
          [antd/option {:key id} name])]
       [:p "Select a template"]

       [:hr]
       (select-template-values @current-template! {:on-change on-change-num})

       [:hr]
       (select-template-values @current-template! {:on-change on-change-den})])))
