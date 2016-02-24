(ns prop-shop.pre-load
  "Provides a central namespace to define application initialization behavior."
  {:author "Justin Timbers"}
  (:require [prop-shop.data.base :as b]))


(defn load-data
  "Persists default data to provide a base set of entities."
  {:added "0.1"}
  []
  (let [org {:name "Mammoth Erection" :type :organization :uuid #uuid "568c78c0-fe6c-4737-8586-5bdee73438bb"}
        org-id (:id (if-not (b/get-entity-by-uuid (:uuid org)) (b/add-entity org)))
        app1 {:name "PropShop" :type :application :uuid #uuid "5695af9b-a061-4199-93ff-6c6ddd0e0c40" :organization org-id}
        app2 {:name "HowToo" :type :application :uuid #uuid "5695afb0-4b54-44c2-aa98-603ffd8b5113" :organization org-id}
        user1 {:name "Dan" :type :user :uuid #uuid "568c78d7-09ad-4730-b468-7e2026e559cf" :organization org-id}
        user2 {:name "Justin" :type :user :uuid #uuid "568c7cf8-1eb5-4099-aa65-70367594a920" :organization org-id}]
    (if-not (b/get-entity-by-uuid (:uuid app1)) (b/add-entity app1))
    (if-not (b/get-entity-by-uuid (:uuid app2)) (b/add-entity app2))
    (if-not (b/get-entity-by-uuid (:uuid user1)) (b/add-entity user1))
    (if-not (b/get-entity-by-uuid (:uuid user2)) (b/add-entity user2))))



