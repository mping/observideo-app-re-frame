(ns observideo.renderer.subs
  (:require [re-frame.core :as rf :refer [reg-sub subscribe]]))

;;;;
;; UI

(rf/reg-sub :ui/active-tab (fn [db _] (:ui/tab db)))
(rf/reg-sub :ui/timestamp (fn [db _] (:ui/timestamp db)))

;;;;
;; Main

(rf/reg-sub :videos/videos-folder (fn [db _] (:videos/videos-folder db)))
(rf/reg-sub :videos/videos-list (fn [db _] (:videos/videos-list db)))