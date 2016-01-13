(ns prop-shop.pre-load
  "Provides a central namespace to define application initialization behavior."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(defn load-data
  "Persists default data to provide a base set of entities."
  {:added "0.1"}
  []
  (let [organization {:name "Mammoth Erection" :type :organization :uuid #uuid "568c78c0-fe6c-4737-8586-5bdee73438bb"}
        organization-id (:id (if-not (b/get-entity-by-uuid (:uuid organization)) (b/add-entity organization)))
        user1 {:name "Dan" :type :user :uuid #uuid "568c78d7-09ad-4730-b468-7e2026e559cf" :organization organization-id}
        user2 {:name "Justin" :type :user :uuid #uuid "568c7cf8-1eb5-4099-aa65-70367594a920" :organization organization-id}]
    (if-not (b/get-entity-by-uuid (:uuid user1)) (b/add-entity user1))
    (if-not (b/get-entity-by-uuid (:uuid user2)) (b/add-entity user2))))



