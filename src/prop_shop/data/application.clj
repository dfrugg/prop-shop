(ns prop-shop.data.application
  "Provides data access and manipulation for Application resources."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(def application-path-map {})


(defn add-application
  "Persists a new Application with the provided name."
  {:added "0.1"}
  ([name organization-uuid] (add-application name organization-uuid application-path-map))
  ([name organization-uuid path-map]
    (b/add-entity
      {:name name
       :type :application
       :organization (b/uuid->id organization-uuid)}
      path-map)))

