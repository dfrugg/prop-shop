(ns prop-shop.data.user-test
  (:require [clojure.test :refer :all]
            [prop-shop.data.user :refer :all]
            [prop-shop.data.organization :refer :all]))

(def my-org (add-organization "My Organization"))

(def your-org (add-organization "Your Organization"))

(deftest test-adding-user
  (testing "Testing adding users"
    (let [user (add-user "Bob" (:uuid my-org))
          same-user (get-user (:uuid user))]
      (is (:id user))
      (is (:name user))
      (is (:uuid user))
      (is (= (:id user) (:id same-user)))
      (is (= (:name user) (:name same-user)))
      (is (= (:uuid user) (:uuid same-user))))))


(deftest test-adding-user-with-org
  (testing "Testing adding users"
    (let [user (add-user "Jim" (:uuid your-org))
          users (get-users-by-organization your-org)]
      (is (:id user))
      (is (:name user))
      (is (:uuid user))
      (is (= 1 (count users)))
      (is (= (:id user) (:id (first users))))
      (is (= (:name user) (:name (first users))))
      (is (= (:uuid user) (:uuid (first users)))))))



