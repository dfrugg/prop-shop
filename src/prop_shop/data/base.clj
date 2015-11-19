(ns prop-shop.data.base
  "Provides a central namespace to resolve application configuration values through.
   Each configuration value should have a corresponding function that returns it's
   value."
  {:author "Daniel Rugg"}
  (:require [datomic.api :as d]
            [prop-shop.config :as config]))

;; Define A Connection & Load Schema
(def conn
  "The Datomic database connection used to persist and retrieve data."
  (let [uri (config/database-uri)]
            (d/create-database uri)
            (d/connect uri)))
