(ns prop-shop.data.base
  "Provides a central namespace to resolve application configuration values through.
   Each configuration value should have a corresponding function that returns it's
   value."
  (:require [clojure.java.io :as io]
            [datomic.api :as d]
            [prop-shop.config :as config])
  (:import datomic.Util
           java.util.UUID))

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


(defn uuid->id
  "Takes the UUID of an entity and returns the internal id."
  [uuid]
  (d/q '[:find ?e .
         :in $ ?u
         :where [?e :uuid ?u]]
    (db)
    uuid))


(defn assoc-if
  "Will associate the result of a function to a key on the given map if the key is not already there."
  [m k f]
  (if (k m) m (assoc m k (f))))


(defn ensure-db
  "Ensures that the db database value is on the given map."
  [opts]
  (assoc-if opts :db db))


(defn ensure-path-map
  "Ensures that the path-map map is on the given map."
  [opts]
  (assoc-if opts :path-map hash-map))


(defn ->opts
  "Ensures that all values are on the given options map."
  [opts]
  (-> opts
      ensure-db
      ensure-path-map))


(defn opts?
  "Checks if a map could possibly be the options."
  [m]
  (and (map? m)
       (or (empty? m)
           (:db m)
           (:path-map m))))


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
  ([entity-id] (resolve-entity-id {} entity-id))
  ([opts entity-id]
    (let [{:keys [db path-map] :as reqs} (->opts opts)]
      (reduce-entity (d/entity db entity-id) path-map))))


(defn resolve-entity-ids
  "Takes a collection of internal IDs of entities, reducing instructions, and an optional
   database value reference, and resolves it to a map of the entity."
  ([entity-ids] (resolve-entity-ids {} entity-ids))
  ([opts entity-ids]
    (let [{:keys [db] :as reqs} (ensure-db opts)]
      (map #(resolve-entity-id reqs %) (map first entity-ids)))))


(defn query
  ([statement] (query {} statement))
  ([opts statement]
    (let [{:keys [db] :as reqs} (ensure-db opts)
          results (d/q statement db)]
      (resolve-entity-ids reqs results))))


(defn query-with-args
  ([opts-or-statement & args]
   (if (opts? opts-or-statement)
     (let [{:keys [db] :as reqs} (ensure-db opts-or-statement)
           statement (first args)
           values (next args)]
       (resolve-entity-ids reqs (apply d/q statement db values)))
     (let [{:keys [db] :as reqs} (ensure-db {})]
       (resolve-entity-ids reqs (apply d/q opts-or-statement db args))))))


(defn transact->entity
  ([trans temp-id] (transact->entity {} trans temp-id))
  ([opts trans temp-id]
    (let [temp-ids (:tempids @trans)
          db (:db-after  @trans)
          reqs (assoc opts :db db)
          id (d/resolve-tempid db temp-ids temp-id)]
      (resolve-entity-id reqs id))))


(defn add-entity
  "Takes an entity and persists it.  The active period for the entity is set from
   as far in the past to as far in the future as possible."
  {:added "0.1"}
  ([entity] (add-entity {} entity))
  ([opts entity]
    (let [temp-id (d/tempid :db.part/user)
          trans (d/transact conn [(merge entity
                                   {:db/id temp-id
                                    :uuid (d/squuid)
                                    :active-on min-date
                                    :inactive-on max-date})])]
      (transact->entity opts trans temp-id))))


(defn get-entities-by-type
  "Retrieves all entities of the provided type."
  {:added "0.1"}
  ([type] (get-entities-by-type {} type))
  ([opts type]
    (query-with-args opts
                     '{:find [?e]
                       :in [$ ?t]
                       :where [[?e :type ?t]]}
                     type)))
(defn uuid?
  [value]
  (or (string? value)
      (instance? java.util.UUID value)))


(defn ->id
  [value]
  (cond
   (number? value) value
   (instance? UUID value) (uuid->id value)
   (string? value) (uuid->id (UUID/fromString value))
   (nil? value) nil
   :else (if (:id value) (:id value) (->id (:uuid value)))))


(defn get-entities-by-type-and-org
  "Retrieves all entities of the provided type."
  {:added "0.1"}
  ([type org] (get-entities-by-type {} type))
  ([opts type org]
    (query-with-args opts
                     '{:find [?e]
                       :in [$ ?t ?o]
                       :where [[?e :type ?t]
                               [?e :organization ?o]]}
                     type
                     (->id org))))


(defn get-entity-by-uuid
  "Takes the UUID of an entity and an optional database value and returns the entity.
   If the database value is not provided, the latest will be used."
  {:added "0.1"}
  ([uuid] (get-entity-by-uuid {} uuid))
  ([opts uuid]
    (first
      (query-with-args opts
                       '{:find [?e]
                         :in [$ ?u]
                         :where [[?e :uuid ?u]]}
                       uuid))))


(defn update-entity-by-id
  "Takes the internal ID of an entity and a map of attributes and values
   and updates those attribute values in the entity."
  {:added "0.1"}
  ([id data] (update-entity-by-id {} id data))
  ([opts id data]
    (d/transact conn [(merge {:db/id id} data)])
    (resolve-entity-id opts id)))


(defn update-entity-by-uuid
  "Takes the UUID of an entity and a map of attributes and values and updates those
   attribute values in the entity."
  {:added "0.1"}
  ([uuid data] (update-entity-by-uuid {} uuid data))
  ([opts uuid data]
    (update-entity-by-id opts (uuid->id uuid) data)))


(defn deactivate-entity
  "Takes the UUID of an entity and an optional date, and sets the inactive date of the entity.
   If the date is not provided, the current date and time will be used."
  {:added "0.1"}
  ([uuid] (deactivate-entity {} uuid (java.util.Date.)))
  ([opts-or-uuid uuid-or-date]
    (if (map? opts-or-uuid)
      (deactivate-entity opts-or-uuid uuid-or-date (java.util.Date.))
      (deactivate-entity {} opts-or-uuid uuid-or-date)))
  ([opts uuid date]
    (update-entity-by-uuid opts uuid {:inactive-on date})))
