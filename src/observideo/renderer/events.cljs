(ns observideo.renderer.events
  (:require
   [re-frame.core :as rf]
   ;[day8.re-frame.tracing :refer-macros [fn-traced]]
   [observideo.renderer.interceptors :as interceptors]))

(def demo-template {:id         (random-uuid)
                    :name       "Demo"
                    :interval   15
                    :attributes {"Peer"   ["Alone" "Adults" "Peers" "Adults and Peers" "N/A"]
                                 "Gender" ["Same" "Opposite" "Both" "N/A"]
                                 "Type"   ["Roleplay" "Rough and Tumble" "Exercise"]}})

(defn empty-db []
  {:ui/tab            :videos
   :ui/timestamp      (str (js/Date.))

   ;; videos list is a vec because they are in the filesystem
   :videos/folder     nil
   :videos/list       nil
   :videos/current    nil

   ;; templates are keyed by :id because it facilitates CRUD operations
   :templates/list    {(:id demo-template) demo-template}
   :templates/current nil})

;;;;
;; Core events

(rf/reg-event-db
  :db/initialize
  (fn [_ _] (empty-db)))

(rf/reg-event-db
  :db/load
  (fn [db server-db] (merge db server-db)))

;;;;
;; IPC events

(rf/reg-event-db
  :main/update-videos
  (fn [db [_ {:keys [folder videos]}]] (assoc db :videos/folder folder :videos/list videos)))

;;;;
;; User events


(rf/reg-event-db
  :ui/ready
  [interceptors/ipc2main-interceptor]
  (fn [db _] db))

;;;;
;; videos
(rf/reg-event-db
  :ui/update-videos-folder
  [interceptors/ipc2main-interceptor]
  (fn [db [_ folder]]
    (assoc db :videos/folder folder)))

(rf/reg-event-db
  :ui/select-video
  (fn [db [_ video]] (assoc db :videos/current video)))

(rf/reg-event-db
  :ui/deselect-video
  (fn [db [_ _]] (assoc db :videos/current nil)))

(rf/reg-event-db
  :ui/change-active-tab
  (fn [db [_ tab]] (assoc db :ui/tab tab)))

;;;;
;; templates

(rf/reg-event-db
  :ui/add-template
  (fn [db [_ template]]
    (let [id (or (:id template) (random-uuid))]
      (assoc-in db [:templates/list id] template))))

(rf/reg-event-db
  :ui/edit-template
  (fn [db [_ template]]
    (assoc db :templates/current template #_(get-in db [:templates/list (:id template)]))))

(rf/reg-event-db
  :ui/update-template
  (fn [db [_ {:keys [id] :as template}]]
    (js/console.log ">>" id template)
    (-> db
      (update-in [:templates/list id] template)
      #_(update-in [:templates/current] template))))

(rf/reg-event-db
  :ui/delete-template
  (fn [db [_ template]]
    (let [id (:id template)]
      (dissoc db :templates/list id))))

(rf/reg-event-db
  :ui/deselect-template
  (fn [db [_ _]] (assoc db :templates/current nil)))
