(ns prop-shop.data.base
  "Provides a central namespace to resolve application configuration values through.
   Each configuration value should have a corresponding function that returns it's
   value."
  (:require [clojure.java.io :as io]
            [datomic.api :as d]
            [prop-shop.config :as config])
  (:import datomic.Util))

;; Define A Connection & Load Schema
(def conn
  "The Datomic database connection used to persist and retrieve data."
  (let [uri (config/database-uri)]
            (d/create-database uri)
            (d/connect uri)))


(def max-date
  "Provides a Date as far in the future as possible.  For used with inactive-on value operations that do not expire."
  (java.util.Date. (Long/MAX_VALUE)))


(def min-date
  "Provides a Date as far in the past as possible.  For used with active-on value operations that always exist."
  (java.util.Date. (Long/MIN_VALUE)))


;; Load The Data Schema Into Datomic
(doseq [schematoms (Util/readAll (io/reader (io/resource "schema.edn")))]
  (d/transact conn schematoms))

;; Begin Defining Helper Functions

(defn db
  "Gets the latest value of the database to use in queries and such."
  []
  (d/db conn))


(defn reduce-entity
  "Takes a given entity and reduces the data within it according to the instructions
   within the path-map.  The path-map instructions can be the following:

  {:field nil}          ;;If field is primitive it is included.  If field is reference it is not included.
  {:field :ignore}      ;;Field is ignored.
  {:field {}}           ;;If field is reference the entity is resolved with only primitive fields
  {:field {:field ...}} ;;If field is reference the entity is with the embedded path-map instructions.
  {:field :id}          ;;If field is reference only the id of the entity is included as a map key/value {:id 1234}.
  {:* ...}              ;;Allows the use of the wild card to apply rule to any non-defined field.  For example,
                        ;;{:field :id} is the same as {:field {:* :ignore}}"
  [entity path-map]
  (into {:id (:db/id entity)}
    (map (fn [[k v]]
           (let [flag (or (k path-map) (:* path-map))]
	           (cond
               (= :ignore flag) nil
               (instance? datomic.Entity v)
	               (cond
	                 (= flag :id) [k {:id (:db/id v)}]
	                 (map? flag) [k (reduce-entity v flag)]
                   :else nil)
                 (and (set? v) (instance? datomic.Entity (first v)))
	                 (cond
	                   (= flag :id) [k (map #(hash-map :id (:db/id %)) v)]
	                   (map? flag) [k (map #(reduce-entity % flag) v)]
	                   :else nil)
               :else [k v])))
          entity)))


(defn resolve-entity-id
  "Takes the internal ID of an entity, reducing instructions, and an optional
   database value reference, and resolves it to a map of the entity."
  ([entity-id path-map] (resolve-entity-id path-map (db)))
  ([entity-id path-map db]
    (reduce-entity (d/entity db entity-id) path-map)))


(defn resolve-entity-ids
  "Takes a collection of internal IDs of entities, reducing instructions, and an optional
   database value reference, and resolves it to a map of the entity."
  ([entity-ids path-map] (resolve-entity-ids entity-ids path-map (db)))
  ([entity-ids path-map db]
    (map #(resolve-entity-id % path-map db) (map first entity-ids))))


(defn query
  ([db path-map statement]
    (let [results (d/q statement db)]
      (resolve-entity-ids results path-map db)))
  ([db path-map statement & args]
    (let [results (apply d/q statement db args)]
      (resolve-entity-ids results path-map db))))

(defn query!
  ([path-map statement]
    (let [results (d/q statement (db))]
      (resolve-entity-ids results path-map db)))
  ([path-map statement & args]
    (let [results (apply d/q statement (db) args)]
      (resolve-entity-ids results path-map db))))


(defn transact->entity
  [trans temp-id path-map]
  (let [temp-ids (:tempids @trans)
        db (:db-after  @trans)
        id (d/resolve-tempid db temp-ids temp-id)]
    (resolve-entity-id id path-map db)))


(defn add-entity
  "Takes an entity and persists it.  The active period for the entity is set from
   as far in the past to as far in the future as possible."
  {:added "0.1"}
  [entity]
  (let [temp-id (d/tempid :db.part/user)]
    (-> (d/transact conn [(merge entity
                            {:db/id temp-id
                             :uuid (d/squuid)
                             :active-on min-date
                             :inactive-on max-date})])
      (transact->entity temp-id {}))))


(defn get-entities-by-type
  "Retrieves all entities of the provided type."
  {:added "0.1"}
  ([type] (get-entities-by-type (db) type))
  ([db type]
   (query db
          {}
          '{:find [?e]
            :in [$ ?t]
            :where [[?e :type ?t]]}
          type)))


(defn get-entity-by-uuid
  "Retrieves the entity with the provided UUID."
  {:added "0.1"}
  ([uuid] (get-entity-by-uuid (db) uuid))
  ([db uuid]
   (query db
          {}
          '{:find [?e]
            :in [$ ?u]
            :where [[?e :uuid ?u]]}
          uuid)))
