(ns observideo.renderer.subs
  (:require [re-frame.core :as rf :refer [reg-sub subscribe]]))

;;;;
;; UI

(rf/reg-sub :ui/active-tab (fn [db _] (:ui/tab db)))
(rf/reg-sub :ui/timestamp (fn [db _] (:ui/timestamp db)))

;;;;
;; Main

(rf/reg-sub :main/videos-folder (fn [db _] (:main/videos-folder db)))
(rf/reg-sub :main/videos-list (fn [db _] (:main/videos-list db)))