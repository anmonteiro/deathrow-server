(defproject deathrow-server "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.8"]
                 [congomongo "0.4.4"]
                 [ring/ring-json "0.3.1"]
                 [ring "1.2.1"]
                 [ring-cors "0.1.2"]
                 [ring/ring-jetty-adapter "1.2.1"]]
  :main deathrow-server.handler
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler deathrow-server.handler/app}
  :uberjar-name "deathrow-server-0.1.0-standalone.jar"
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
