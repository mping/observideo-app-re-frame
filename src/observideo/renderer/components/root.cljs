(ns observideo.renderer.components.root
  (:require
   [cljs.core.async :as async :refer [go <! put!]]
   [com.wsscode.common.async-cljs :refer [<?maybe]]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [observideo.renderer.components.antd :as antd]
   [observideo.renderer.components.settings :as settings]
   [observideo.renderer.components.videos :as videos]
   [observideo.renderer.components.queries :as queries]))

;;;;;;;;;
;; layout


(defrouter RootRouter [_ _]
  {:router-targets [settings/Settings videos/Videos queries/Queries]})

(def ui-root-router (comp/factory RootRouter))

(defsc Root [this {:root/keys                                           [router folder]
                   ;; TODO fixme: this required to ensure current-route gets refreshed
                   :com.fulcrologic.fulcro.routing.dynamic-routing/keys [id]
                   :as                                                  props}]
  {:query         [{:root/router (comp/get-query RootRouter)}
                   {:root/folder (comp/get-query videos/Videos)}
                   ;; TODO fixme: this required to ensure current-route gets refreshed
                   :com.fulcrologic.fulcro.routing.dynamic-routing/id]
   :initial-state {:root/router {}}}

  (let [[current-route] (dr/current-route this RootRouter)
        name (:folder/id folder)]
    (antd/layout
      {:hasSider true}                                      ;;apparently :style doesnt work
      (antd/sider
        {:collapsible true :theme "light"}
        (dom/div {:className "logo"})

        (antd/menu
          {:mode "inline" :theme "light" :defaultSelectedKeys ["settings"]}
          (antd/menuitem {:key     "settings"
                          :onClick #(dr/change-route this ["settings"])}
            (antd/icon {:type "setting"})
            (dom/span "Settings"))

          (antd/menuitem {:key     "videos"
                          :onClick #(dr/change-route this ["videos" (or name "empty")])}
            (antd/icon {:type "video-camera"})
            (dom/span "Videos"))

          (antd/menuitem {:key     "queries"
                          :onClick #(dr/change-route this ["queries" folder])}
            (antd/icon {:type "bar-chart"})
            (dom/span "Queries"))))

      ;; sidebar > content
      (antd/content
        {:style {:background "#fff" :padding "10px"}}

        ;;breadcrumbs
        (antd/breadcrumb
          nil
          (antd/breadcrumb-item nil (str current-route))
          (and (= "videos" current-route)
            (antd/breadcrumb-item nil (str name)))
          (and (= "videos" current-route)
            ;video
            (antd/breadcrumb-item nil (videos/fname "/(:filename video)"))))

        ;; main panel switch
        ;;((comp/factory videos/Root) props)
        (ui-root-router router)))))
