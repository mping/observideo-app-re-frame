(ns observideo.renderer.subs
  (:require
   [clojure.set :as set]
   [re-frame.core :as rf :refer [reg-sub subscribe]]
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

(rf/reg-sub-raw :videos/current-section
  (fn [db _]
    (reaction
      (get-in @db [:videos/current :current-section]))))

(rf/reg-sub-raw :videos/current-template
                (fn [db _]
                  (reaction
                   (let [template-id (get-in @db [:videos/current :template-id])]
                     (get-in @db [:templates/all template-id])))))

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


;;;;
;; query helpers

(defn- matches? [obs qry]
  (if (empty? (filter identity (vals qry)))
    true
    (let [prune        #(reduce-kv (fn [m k v]
                                     (if (nil? v)
                                       m
                                       (assoc m k v))) {} %)
          observations (prune obs)
          query        (prune qry)]
      ;; maps are converted to #{[k v] .. [k v]}
      (js/console.log observations)
      (js/console.log query)
      (set/subset? (set query)
                   (set observations)))))

(comment
  (matches? {"Peer" "Alone" "Gender" "Male"} {"Peer" "Alone" "Gender" nil})) ;;true

(defn- run-query
  "Returns a vector [query num-of-observations videos]
   Example: [ (\"Alone, Group\") 5 (\"SampleVideo.mpg\" \"OtherVideo.mpg\")]
"
  [videos aggregator query]
  (->> videos
       (map (fn [{:keys [filename observations]}]
              ;; [filename matches]
              [filename (count (filter #(matches? % query) observations))]))
       ;; sum all matching results
       (reduce (fn [[tot vids] [f c]]
                 [(+ tot c) (conj vids f)])
               [0 []])
       ;; Add the query
       (cons (->> query vals (filter identity)))))
               

#_
(let [db @re-frame.db/app-db      
      {:keys [template-id aggregator top bottom]} (:query/current db)
      videos (:videos/all db)
      videos (filter #(= template-id (:template-id %)) (vals videos))]
  (def *v videos)
  (def *q top)
  (run-query videos aggregator top))

;; TODO FIXME
;; should actually return
;; [query {cnt1 video1 cnt2 video2 ...}]

(rf/reg-sub :query/result
  (fn [db _]
    (let [{:keys [template-id aggregator top bottom]} (:query/current db)
          videos (:videos/all db)
          videos (filter #(= template-id (:template-id %)) (vals videos))]
      {:top    (run-query videos aggregator top)
       :bottom [(vals bottom) 3 (keys (get-in db [:videos/all]))]})))
