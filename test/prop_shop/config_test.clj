(ns prop-shop.config-test
  (:require [clojure.test :refer :all]
            [prop-shop.config :refer :all]))

; Test database-uri function

(deftest test-database-uri-default
  (testing "Database URI Configuration Default Value"
    (is (= (database-uri) "datomic:mem://prop-shop"))))

(deftest test-database-uri-override
  (testing "Database URI Configuration Overridden Value"
    (is (= (database-uri) "datomic:mem://prop-shop"))
    (let [val "Override Value"]
      (System/setProperty "prop-shop.database.uri" val)
      (is (= (database-uri) val)))
    (System/clearProperty "prop-shop.database.uri")
    (is (= (database-uri) "datomic:mem://prop-shop"))))
