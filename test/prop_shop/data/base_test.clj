(ns prop-shop.data.base-test
  (:require [clojure.test :refer :all]
            [prop-shop.data.base :refer :all]))


(deftest test-database-connection
  (testing "Database Connection"
    (is (not (nil? conn)))))
