(ns observideo.renderer.events
  (:require
   [re-frame.core :as rf]
   ;[day8.re-frame.tracing :refer-macros [fn-traced]]
   [observideo.common.datamodel :as datamodel]
   [observideo.renderer.interceptors :as interceptors]))

(def demo-template datamodel/demo-template)

;;;;
;; Core events

(rf/reg-event-db
  :db/initialize
  (fn [_ _] (datamodel/empty-db)))

(rf/reg-event-db
  :db/load
  (fn [db server-db] (merge db server-db)))

(rf/reg-event-db
  :db/reset
  (fn [db [_ server-db]]
    (or server-db db)))
    ;(merge db server-db)))

;;;;
;; IPC events

(rf/reg-event-db
  :main/update-videos
  [interceptors/queue-save-db]
  (fn [db [_ {:keys [folder videos]}]]
    (assoc db :videos/folder folder
              :videos/all (reduce (fn [m v] (assoc m (:filename v) v)) {} videos))))

;;;;
;; User events

(rf/reg-event-db
  :ui/ready
  [interceptors/event->ipc]
  (fn [db _] db))

;;;;
;; videos

(rf/reg-event-db
  :ui/update-videos-folder
  [interceptors/event->ipc interceptors/queue-save-db]
  (fn [db [_ folder]]
    (assoc db :videos/folder (:folder folder))))

(rf/reg-event-db
  :ui/select-video
  ;; TODO assoc only video id
  (fn [db [_ video]] (assoc db :videos/current video)))

(rf/reg-event-db
  :ui/deselect-video
  (fn [db [_ _]] (assoc db :videos/current nil)))

(rf/reg-event-db
  :ui/change-active-tab
  (fn [db [_ tab]] (assoc db :ui/tab tab)))


;;;;
;; video editing

(defn- count-observations [duration step-interval]
  (+ (int (/ duration step-interval))
    (if (> (mod duration step-interval) 0) 1 0)))

(defn- make-empty-observations [template n]
  (->> (range)
    (take n)
    (map (fn [_]
           (let [attrs (:attributes template)]
             ;; create a mapping {"name" => nil}
             (reduce-kv (fn [m k _] (assoc m k nil)) {} attrs))))
    (vec)))

(rf/reg-event-db
  :ui/update-current-video-template
  [interceptors/queue-save-db]
  (fn [db [_ id]]
    (let [current-video      (:videos/current db)
          template           (get-in db [:templates/all id])
          template-interval  (:interval template)
          total-observations (count-observations (:duration current-video) template-interval)
          new-observations   (make-empty-observations template total-observations)
          updated-video      (assoc current-video :template-id id :observations new-observations)
          fullpath           (:filename updated-video)]
      (-> db
        (assoc-in [:videos/current] updated-video)
        (assoc-in [:videos/all fullpath] updated-video)))))

(rf/reg-event-db
  :ui/update-current-video-section
  (fn [db [_ time index]]
    (let [current-video (:videos/current db)
          updated-video (assoc current-video :current-section {:time time :index index})
          fullpath      (:filename updated-video)]
      (-> db
        (assoc-in [:videos/current] updated-video)
        (assoc-in [:videos/all fullpath] updated-video)))))

(rf/reg-event-db
  :ui/update-current-video-current-section-observation
  [interceptors/queue-save-db]
  (fn [db [_ observation]]
    (let [current-video     (:videos/current db)
          observation-index (get-in current-video [:current-section :index])
          updated-video     (assoc-in current-video [:observations observation-index] observation)
          fullpath          (:filename updated-video)]
      (-> db
        (assoc-in [:videos/current] updated-video)
        (assoc-in [:videos/all fullpath] updated-video)))))

;;;;
;; templates

(rf/reg-event-db
  :ui/add-template
  [interceptors/queue-save-db]
  (fn [db [_ _]]
    (let [new-template (dissoc demo-template :id)
          id           (str (random-uuid))]
      (-> db
        (assoc-in [:templates/all id] (assoc new-template :id id))))))

(rf/reg-event-db
  :ui/edit-template
  (fn [db [_ template]]
    ;; make a copy
    (assoc db :templates/current (merge {} (get-in db [:templates/all (:id template)])))))

(rf/reg-event-db
  :ui/update-template
  [interceptors/queue-save-db]
  (fn [db [_ {:keys [id] :as template}]]
    (-> db
      (assoc-in [:templates/all id] template))))

(rf/reg-event-db
  :ui/update-current-template
  [interceptors/queue-save-db]
  (fn [db [_ template]]
    (-> db
      (assoc-in [:templates/current] template))))

(rf/reg-event-db
  :ui/delete-template
  [interceptors/queue-save-db]
  (fn [db [_ template]]
    (let [id (:id template)]
      (-> db
        (dissoc :templates/all id)))))

(rf/reg-event-db
  :ui/deselect-template
  (fn [db [_ _]] (assoc db :templates/current nil)))
