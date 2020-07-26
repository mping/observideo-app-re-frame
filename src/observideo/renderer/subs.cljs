(ns observideo.renderer.subs
  (:require [re-frame.core :as rf :refer [reg-sub subscribe]]
            [reagent.ratom :as r :refer-macros [reaction]]
            [taoensso.timbre :as log]))

;;;;
;; UI

(rf/reg-sub :ui/active-tab
  (fn [db _] (:ui/tab db)))

;;;;
;; Main

(rf/reg-sub :videos/folder
  (fn [db _] (:videos/folder db)))

(rf/reg-sub :videos/all
  (fn [db _] (:videos/all db)))

(rf/reg-sub :videos/current
  (fn [db _] (:videos/current db)))

(rf/reg-sub-raw :videos/current-template
  (fn [db _]
    (reaction
      (let [template-id (get-in @db [:videos/current :template-id])]
        (get-in @db [:templates/all template-id])))))

(rf/reg-sub-raw :videos/current-section
  (fn [db _]
    (reaction
      (get-in @db [:videos/current :current-section]))))

(rf/reg-sub-raw :videos/current-observation
  (fn [db _]
    (reaction
      (let [{:keys [index]} (get-in @db [:videos/current :current-section])
            current-observation (get-in @db [:videos/current :observations index])]
        current-observation))))

(rf/reg-sub-raw :templates/video-count
  (fn [db _]
    (reaction
      (let [aggr (map :template-id (vals (get @db :videos/all)))
            cnt  (frequencies aggr)]
         cnt))))

(rf/reg-sub :templates/all
  (fn [db _] (:templates/all db)))

(rf/reg-sub :templates/current
  (fn [db _] (:templates/current db)))

;;;;
;; queries

(rf/reg-sub :query/current
  (fn [db _] (:query/current db)))

(rf/reg-sub :query/result
  (fn [db _]
    (let [{:keys [aggregator top bottom]} (:query/current db)]
      (log/info top)
      {:top    [(vals top)    5 (keys (get-in db [:videos/all]))]
       :bottom [(vals bottom) 3 (keys (get-in db [:videos/all]))]})))
