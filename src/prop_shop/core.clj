(ns prop-shop.core
  (:use compojure.core
        ring.adapter.jetty)
  (:require [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.resource :refer [wrap-resource]]
            [prop-shop.pre-load :as p]))


(defroutes main-routes
  (GET "/" [] (response/redirect "main.htm"))
  (route/not-found "<h1>Not Found!</h1>"))

(defn app
  []
  (-> main-routes
      (wrap-resource "public")))

(defn start-server
  []
  (run-jetty (app) {:port 3000}))

(defn -main [& args]
  (p/load-data)
  ; with app running, use http://localhost:3000/ to access page
  (start-server))
