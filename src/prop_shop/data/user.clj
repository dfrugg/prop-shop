(ns prop-shop.data.user
  "Provides data access and manipulation for User resources."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(def user-path-map {})


(defn add-user
  "Persists a new User with the provided name."
  {:added "0.1"}
  ([name organization-uuid] (add-user name organization-uuid user-path-map))
  ([name organization-uuid path-map]
     (b/add-entity
       {:name name
        :type :user}
       path-map)))


(defn get-users
  "Retrieves all Users."
  {:added "0.1"}
  []
  (b/get-entities-by-type :user))


(defn get-user
  "Retrieves an User by its UUID."
  {:added "0.1"}
  [uuid]
  (b/get-entity-by-uuid uuid))


(defn rename-user
	"Renames the User specified by the UUID with the provided name."
	{:added "0.1"}
	[uuid name]
	(b/update-entity-by-uuid uuid {:name name}))


(defn deactivate-user
  "Deactivates the User specified by the UUID."
  {:added "0.1"}
	[uuid]
	(b/deactivate-entity uuid))
