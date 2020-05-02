(ns observideo.common.datamodel
  (:require [spec-tools.data-spec :as ds]
            [clojure.spec.alpha :as s]))

(def demo-template {:id         "fb52dd46-85cc-4864-b11e-44b8a5b28331"
                    :name       "Demo"
                    :interval   15
                    :next-index 3                           ;;monotonic counter to ensure old indexes preserve their value
                    :attributes {"Peer"   {:index 0 :values ["Alone" "Adults" "Peers" "Adults and Peers" "N/A"]}
                                 "Gender" {:index 1 :values ["Same" "Opposite" "Both" "N/A"]}
                                 "Type"   {:index 2 :values ["Roleplay" "Rough and Tumble" "Exercise"]}}})



(def demo-video {:filename        "/home/mping/Download … deo_720x480_30mb.mp4"
                 :duration        183.318
                 :info            {:a "changeme"}
                 :md5sum          "changeme"
                 :size            31551484
                 :missing?         false
                 :current-section {:time 0, :index 0}
                 :observations    [{"Peer" nil "Gender" "Same" "Type" "Exercise"}, {}]
                 :template-id     "7dd2479d-e829-4762-a0ac-de51a68461b5"})

;;;;
;; Specs

(def attribute-spec
  (ds/spec {:name ::atribute
            :spec {:index int? :values [string?]}}))

(def template-spec
  (ds/spec {:name ::template
            :spec {:id         string?
                   :name       string?
                   :interval   int?
                   :next-index int?
                   :attributes (s/map-of string? attribute-spec)}}))

(def section-spec
  (ds/spec {:name ::section
            :spec {:time number? :index int?}}))

(def observation-spec
  (ds/spec {:name ::observation
            :spec (s/map-of string? (s/nilable string?))}))

(def video-spec
  (ds/spec {:name ::video
            :spec {:filename        string?
                   :duration        number?
                   :info            any?
                   :md5sum          string?
                   :size            int?
                   :missing?        boolean?
                   :current-section section-spec
                   :observations    [observation-spec]
                   :template-id     string?}}))

(defn empty-db []
  {:observideo/filename nil
   :ui/tab              :videos

   ;; videos list is a vec because they are in the filesystem
   :videos/folder       nil ;;string
   :videos/all          nil ;;map {filename > video}
   :videos/current      nil ;;video

   ;; templates are keyed by :id because it facilitates CRUD operations
   :templates/all       {(:id demo-template) demo-template} ;; {uuid -> template}
   :templates/current   nil})                               ;; template


(defn merge-db [new-db opts])
