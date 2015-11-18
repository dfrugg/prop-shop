(ns prop-shop.config
  "Provides a central namespace to resolve application configuration values through.
   Each configuration value should have a corresponding function that returns it's
   value."
  {:author "Daniel Rugg"})


; Database Configuration Lookup Methods

(defn database-uri
  "Gets the URI used to connect to the Datomic datastore."
  {:added "0.1"}
  []
  (or (System/getProperty "prop-shop.database.uri")
      "datomic:mem://prop-shop"))
