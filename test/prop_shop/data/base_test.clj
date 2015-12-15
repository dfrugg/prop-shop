(ns prop-shop.data.base-test
  (:require [clojure.test :refer :all]
            [prop-shop.data.base :refer :all]))


(deftest test-database-connection
  (testing "Database Connection"
    (is (not (nil? conn)))))

; Needed to fake out the reduce-entity method checks
(deftype E [contents]
    clojure.lang.IPersistentMap
  (assoc [_ k v]
    (E. (.assoc contents k v)))
  (assocEx [_ k v]
    (E. (.assocEx contents k v)))
  (without [_ k]
    (E. (.without contents k)))

  java.lang.Iterable
  (iterator [this]
    (.iterator contents))

  clojure.lang.Associative
  (containsKey [_ k]
    (.containsKey contents k))
  (entryAt [_ k]
    (.entryAt contents k))

  clojure.lang.IPersistentCollection
  (count [_]
    (count contents))
  (cons [_ o]
    (E. (.cons contents o)))
  (empty [_]
    (empty contents))
  (equiv [_ o]
    (and (isa? (class o) E)
         (.equiv contents o)))

  clojure.lang.Seqable
  (seq [_]
    (seq contents))

  clojure.lang.ILookup
  (valAt [_ k]
    (.valAt contents k))
  (valAt [_ k not-found]
    (.valAt contents k not-found))

  datomic.Entity)

(def mock-reduce-entity-data
  "Provides a tree data structure to be used to test the reduce-entity method."
  (E.
    {:db/id 34
     :name "Juan Valdez"
     :age 34
     :married? true
     :children #{(E. {:db/id 44 :name "Dos Valdez"})
                 (E. {:db/id 54 :name "Lil Valdez"})
                 (E. {:db/id 64 :name "Exxon Valdez"})}
     :lucky-numbers #{7 12 18 22 31 55}
     :aliases #{"John" "Joowan" "Tabitha"}
     :father (E. {:db/id 74 :name "Bob Valdez"})}))

(comment
  "As a reminder, this is what to test for:

  {:field nil}          ;;If field is primitive it is included.  If field is reference it is not included.
  {:field :ignore}      ;;Field is ignored.
  {:field {}}           ;;If field is reference the entity is resolved with only primitive fields
  {:field {:field ...}} ;;If field is reference the entity is with the embedded path-map instructions.
  {:field :id}          ;;If field is reference only the id of the entity is included as a map key/value {:id 1234}.
  {:* ...}              ;;Allows the use of the wild card to apply rule to any non-defined field.  For example,
                        ;;{:field :id} is the same as {:field {:* :ignore}}")

(deftest test-reduce-entity-empty-map
  (testing "Testing reduce-entity with empty path-map"
    (let [result (reduce-entity mock-reduce-entity-data {})]
      (is (:id result))
      (is (:name result))
      (is (:age result))
      (is (:married? result))
      (is (nil? (:children result)))
      (is (= 6 (count (:lucky-numbers result))))
      (is (= 3 (count (:aliases result))))
      (is (nil? (:father result))))))


(deftest test-reduce-entity-ignores
  (testing "Testing reduce-entity with empty path-map"
    (let [result (reduce-entity mock-reduce-entity-data {:name :ignore :aliases :ignore})]
      (is (:id result))
      (is (nil? (:name result)))
      (is (:age result))
      (is (:married? result))
      (is (nil? (:children result)))
      (is (= 6 (count (:lucky-numbers result))))
      (is (nil?  (:aliases result)))
      (is (nil? (:father result))))))


(deftest test-reduce-entity-wildcard-ignores
  (testing "Testing reduce-entity with empty path-map"
    (let [result (reduce-entity mock-reduce-entity-data {:* :ignore})]
      (is (:id result))
      (is (nil? (:name result)))
      (is (nil? (:age result)))
      (is (nil? (:married? result)))
      (is (nil? (:children result)))
      (is (nil? (:lucky-numbers result)))
      (is (nil? (:aliases result)))
      (is (nil? (:father result))))))


(deftest test-add-entity
  (testing "Testing the adding data."
    (let [data {:type :test :name "Bobby Su" :encrypted false}
          entity (add-entity data)]
      (is (:id entity))
      (is (:uuid entity))
      (is (:active-on  entity))
      (is (:inactive-on  entity))
      (is (= (select-keys entity (keys data)) data)))))

(deftest test-query!
  (testing "Testing the adding data."
    (let [data {:type :test :name "Bobby Su" :encrypted false}
          q '{:find [?e]
            :in [$ ?t]
            :where [[?e :type ?t]]}
          _ (add-entity data)
          entity (first (query! {} q :test))]
      (is (:id entity))
      (is (:uuid entity))
      (is (:active-on  entity))
      (is (:inactive-on  entity))
      (is (= (select-keys entity (keys data)) data)))))


(deftest test-get-entities-by-type
  (testing "Testing getting entities by their :type."
    (let [data {:type :test :name "Bobby Su" :encrypted false}
          _ (add-entity data)
          entity (first (get-entities-by-type :test))]
      (is (:id entity))
      (is (:uuid entity))
      (is (:active-on  entity))
      (is (:inactive-on  entity))
      (is (= (select-keys entity (keys data)) data)))))


(deftest test-get-entity-by-uuid
  (testing "Testing getting an entity by its UUID."
    (let [data {:type :test :name "Bobby Su" :encrypted false}
          uuid (:uuid (add-entity data))
          entity (get-entity-by-uuid uuid)]
      (is (:id entity))
      (is (:uuid entity))
      (is (:active-on  entity))
      (is (:inactive-on  entity))
      (is (= (select-keys entity (keys data)) data)))))


(deftest test-update-entity-by-uuid
  (testing "Testing updating an entity using its UUID."
    (let [data {:type :test :name "Bobby Su" :encrypted false}
          mod-data {:type :mod-data :name "Su Bobby" :encrypted true}
          entity (add-entity data)
          uuid (:uuid entity)]
      (is (= (select-keys entity (keys data)) data))
      (is (= (select-keys (update-entity-by-uuid uuid mod-data) (keys mod-data)) mod-data)))))


(deftest test-update-entity-by-id
  (testing "Testing updating an entity using its internal ID."
    (let [data {:type :test :name "Bobby Su" :encrypted false}
          mod-data {:type :mod-data :name "Su Bobby" :encrypted true}
          entity (add-entity data)
          id (:id entity)]
      (is (= (select-keys entity (keys data)) data))
      (is (= (select-keys (update-entity-by-id id mod-data) (keys mod-data)) mod-data)))))


(deftest test-uuid->id
  (testing "Testing determining the internal ID of an entity using its UUID."
    (let [entity (add-entity {:type :test})
          id (:id entity)
          uuid (:uuid entity)]
      (is (= (uuid->id uuid) id)))))


(deftest test-deactivate-entity
  (testing "Testing setting the inactive date of an entity."
    (let [data {:type :test :name "Bobby Su" :encrypted false}
          uuid (:uuid (add-entity data))
          date (java.util.Date.)]
      (is (not= (:inactive-on (get-entity-by-uuid uuid)) date))
      (is (= (:inactive-on (deactivate-entity uuid date)) date))
      (is (deactivate-entity uuid date)))))
