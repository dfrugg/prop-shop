(ns prop-shop.data.organization
  "Provides data access and manipulation for Organization resources."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(defn add-organization
  "Adds a new Organization with the provided name."
  {:added "0.1"}
  [name]
  (b/add-entity
    {:name name
     :type :organization}))
