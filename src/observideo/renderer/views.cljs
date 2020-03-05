(ns observideo.renderer.views
  (:require [re-frame.core :as rf]
            [observideo.renderer.components.antd :as antd]
            [observideo.renderer.components.templates :as templates]
            [observideo.renderer.components.queries :as queries]
            [observideo.renderer.components.videos :as videos]
            [clojure.string :as s]))


(defn- fname [path]
  ;;TODO use os.separator
  (subs path (inc (s/last-index-of path "/"))))


(defn selected-tab [active]
  (case active
    :queries queries/ui
    :templates templates/ui
    :videos videos/ui
    ;;default
    [:h1 (str "Unknown: " active)]))

(defn root []
  (let [active  @(rf/subscribe [:ui/active-tab])
        folder  @(rf/subscribe [:videos/folder])
        current @(rf/subscribe [:videos/current])
        vname   (:filename current)]

    [antd/layout {:hasSider true}
     [antd/sider {:collapsible false :collapsed true :theme "light"}
      [:div.logo
       [antd/menu {:mode "inline" :theme "light" :defaultSelectedKeys [(name active)]}
        [antd/menuitem {:key "videos" :onClick #(rf/dispatch [:ui/change-active-tab :videos])}
         [antd/videos-icon]
         [:span "Videos"]]
        [antd/menuitem {:key "templates" :onClick #(rf/dispatch [:ui/change-active-tab :templates])}
         [antd/templates-icon]
         [:span "Templates"]]
        [antd/menuitem {:key "queries" :onClick #(rf/dispatch [:ui/change-active-tab :queries])}
         [antd/queries-icon]
         [:span "Queries"]]]]]
     
     [antd/content {:style {:background "#fff" :padding "10px"}}
      [antd/breadcrumb
       [antd/breadcrumb-item active]
       (when (= :videos active)
         [antd/breadcrumb-item
          [:a {:onClick #(rf/dispatch [:ui/deselect-video])} folder]])
       (when (and (= :videos active) vname)
         [antd/breadcrumb-item (fname vname)])]
      [(selected-tab active)]]]))

(defn ui
  []
  [root])
