(ns observideo.renderer.views
  (:require [re-frame.core :as rf :refer [subscribe dispatch]]
            [observideo.renderer.components.antd :as antd]
            [observideo.renderer.components.settings :as settings]
            [observideo.renderer.components.queries :as queries]
            [observideo.renderer.components.videos :as videos]))

(defn selected-tab [active]
  (case active
    :queries queries/ui
    :settings settings/ui
    :videos videos/ui
    ;;default
    [:h1 (str "Unknown: " active)]))

(defn root []
  (let [active @(rf/subscribe [:ui/active-tab])
        folder @(rf/subscribe [:videos/videos-folder])]

    [antd/layout {:hasSider true}
     [antd/sider {:collapsible true :theme "light"}
      [:div.logo
       [antd/menu {:mode "inline" :theme "light" :defaultSelectedKeys ["settings"]}
        [antd/menuitem {:key "videos" :onClick #(rf/dispatch [:ui/change-active-tab :videos])}
         [antd/icon {:type "video-camera"}]
         [:span "Videos"]]
        [antd/menuitem {:key "settings" :onClick #(rf/dispatch [:ui/change-active-tab :settings])}
         [antd/icon {:type "setting"}]
         [:span "Settings"]]
        [antd/menuitem {:key "queries" :onClick #(rf/dispatch [:ui/change-active-tab :queries])}
         [antd/icon {:type "bar-chart"}]
         [:span "Queries"]]]]]

     [antd/content {:style {:background "#fff" :padding "10px"}}
      [antd/breadcrumb
       [antd/breadcrumb-item active]
       [antd/breadcrumb-item folder]]
      [(selected-tab active)]]]))

(defn ui
  []
  [root])
