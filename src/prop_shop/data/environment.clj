(ns prop-shop.data.environment
  "Provides data access and manipulation for Environment resources."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(defn add-environment
  "Persists a new Environment with the provided name."
  {:added "0.1"}
  [name]
  (b/add-entity
    {:name name
     :type :environment}))


(defn get-environments
  "Retrieves all Environments."
  {:added "0.1"}
  []
  (b/get-entities-by-type :environment))
