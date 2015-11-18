(ns prop-shop.core
  (:use compojure.core
        ring.adapter.jetty)
  (:require [compojure.route :as route]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defroutes main-routes
  (GET "/" [] "<h1>Wassup?</h1>")
  (route/not-found "<h1>Dammit!</h1>"))

(defn app
  []
  (-> main-routes))

(defn start-server
  []
  (run-jetty (app) {:port 3000}))

(defn -main [& args]
  ; with app running, use http://localhost:3000/ to access page
  (start-server))
