(ns prop-shop.data.user
  "Provides data access and manipulation for User resources."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(defn add-user
  "Persists a new User with the provided name."
  {:added "0.1"}
  [name]
  (b/add-entity
    {:name name
     :type :user}))
