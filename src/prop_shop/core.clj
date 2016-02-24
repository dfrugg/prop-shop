(ns prop-shop.core
  (:use compojure.core
        compojure.coercions
        ring.adapter.jetty)
  (:require [compojure.route :as route]
            [ring.util.response :as response]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [prop-shop.pre-load :as p]
            [prop-shop.data.base :as b]))


(defroutes main-routes
  (GET "/" [] (response/redirect "main.htm"))
  (context "/sapi" []
    (context "/app" []
      (POST "/" {body :body}
        (response/response
          (b/add-entity {:path-map {:organization {}}}
                        {:name (:name body)
                         :type :application
                         :organization (b/uuid->id (as-uuid (:organization-uuid body)))})))
      (PUT "/" {body :body}
        (response/response
          (b/update-entity-by-uuid {:path-map {:organization {}}}
                                   (as-uuid (:uuid body))
                                   {:name (:name body)})))
      (DELETE "/:uuid" [uuid :<< as-uuid]
        (response/response
          (b/deactivate-entity {:path-map {:organization {}}}
                               uuid)))
      (GET "/" []
        (response/response
          (b/get-entities-by-type {:path-map {:organization {}}} :application)))
      (GET "/:uuid" [uuid :<< as-uuid]
        (response/response
          (b/get-entity-by-uuid {:path-map {:organization {}}} uuid)))))
  (route/not-found "<h1>Not Found!</h1>"))


(defn app
  []
  (-> main-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-resource "public")))


(defn start-server
  []
  (run-jetty (app) {:port 3000}))


(defn -main [& args]
  (p/load-data)
  ; with app running, use http://localhost:3000/ to access page
  (start-server))
