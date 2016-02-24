(defproject prop-shop "0.1.0-SNAPSHOT"

  :description "FIXME: write description"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}


  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :creds :gpg}}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.datomic/datomic-pro "0.9.5327" :exclusions [joda-time]]
                 [ring "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.4.0"]]

  :plugins [[lein-codox "0.9.0"]
            [lein-ring "0.9.7"]]

  :ring {:handler prop-shop.core/app
         :auto-reload? true
         :auto-refresh? true}

  :main prop-shop.core)
