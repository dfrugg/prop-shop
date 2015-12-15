(ns prop-shop.data.organization
  "Provides data access and manipulation for Organization resources."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(defn add-organization
  "Persists a new Organization with the provided name."
  {:added "0.1"}
  [name]
  (b/add-entity
    {:name name
     :type :organization}))


(defn get-organizations
  "Retrieves all Organizations."
  {:added "0.1"}
  []
  (b/get-entities-by-type :organization))


(defn get-organization
  "Retrieves an Organization by its UUID."
  {:added "0.1"}
  [uuid]
  (b/get-entity-by-uuid uuid))


(defn rename-organization
	"Renames the Organization specified by the UUID with the provided name."
	{:added "0.1"}
	[uuid name]
	(b/update-entity-by-uuid uuid {:name name}))


(defn deactivate-organization
  "Deactivates the Organization specified by the UUID."
  {:added "0.1"}
	[uuid]
	(b/deactivate-entity uuid))
