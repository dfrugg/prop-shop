(ns prop-shop.data.user
  "Provides data access and manipulation for User resources."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(def user-path-map {})


(defn add-user
  "Persists a new User with the provided name."
  {:added "0.1"}
  ([name organization] (add-user name organization user-path-map))
  ([name organization path-map]
     (b/add-entity
       {:path-map path-map}
       {:name name
        :organization (b/->id organization)
        :type :user})))


(defn get-users
  "Retrieves all Users."
  {:added "0.1"}
  ([] (get-users user-path-map))
  ([path-map]
    (b/get-entities-by-type {:path-map path-map} :user)))


(defn get-users-by-organization
  "Retrieves all Users."
  {:added "0.1"}
  ([org] (get-users-by-organization org user-path-map))
  ([org path-map]
    (b/get-entities-by-type-and-org {:path-map path-map} :user org)))


(defn get-user
  "Retrieves an User by its UUID."
  {:added "0.1"}
  ([uuid] (get-user uuid user-path-map))
  ([uuid path-map]
    (b/get-entity-by-uuid {:path-map path-map} uuid)))


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
