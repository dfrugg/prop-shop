(ns prop-shop.data.Property
  "Provides data access and manipulation for Property resources."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(def path-map {})


(def opts
  {
    :path-map path-map
  })


(defn add-property
  "Persists a new Property with the provided name."
  {:added "0.1"}
  [name application-uuid]
  (b/add-entity
    opts
    {:name name
     :type :property
     :application (b/uuid->id application-uuid)}))
