(ns observideo.renderer.events
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-event-fx inject-cofx path after]]
   [cljs.spec.alpha :as s]))


(defn empty-db []
  {:ui/tab         :videos
   :ui/timestamp   (str (js/Date.))

   :videos/folder  nil
   :videos/list    nil
   :videos/current nil})

;;;;
;; Core events

(rf/reg-event-db
  :initialize
  (fn [_ _] (empty-db)))

;;;;
;; IPC events

(rf/reg-event-db
  :main/update-videos
  (fn [db [_ {:keys [folder videos]}]] (assoc db :videos/folder folder :videos/list videos)))

;;;;
;; User events

(rf/reg-event-db
  :ui/update-videos-folder
  (fn [db [_ folder]] (assoc db :videos/folder folder)))

(rf/reg-event-db
  :ui/select-video
  (fn [db [_ video]] (assoc db :videos/current video)))

(rf/reg-event-db
  :ui/deselect-video
  (fn [db [_ _]] (assoc db :videos/current nil)))

(rf/reg-event-db
  :ui/change-active-tab
  (fn [db [_ tab]] (assoc db :ui/tab tab)))
