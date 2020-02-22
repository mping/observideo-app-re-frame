(ns observideo.renderer.events
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-event-fx inject-cofx path after]]
   [cljs.spec.alpha :as s]))


(defn empty-db []
  {:ui/tab             :settings
   :ui/timestamp       (str (js/Date.))

   :main/videos-folder nil
   :main/videos-list nil})

(rf/reg-event-db
  :initialize
  (fn [_ _] (empty-db)))

(rf/reg-event-db
  :ui/update-videos-folder
  (fn [db [_ folder]] (assoc db :main/videos-folder folder)))

(rf/reg-event-db
  :main/update-videos
  (fn [db [_ {:keys [folder videos]}]] (assoc db :main/videos-folder folder :main/videos-list videos)))

(rf/reg-event-db
  :ui/change-active-tab
  (fn [db [_ tab]] (assoc db :ui/tab tab)))
