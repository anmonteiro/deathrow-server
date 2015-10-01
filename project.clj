(defproject deathrow-server "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [congomongo "0.4.6"]
                 [ring/ring-json "0.4.0"]
                 [ring "1.4.0"]
                 [ring-cors "0.1.7"]]
  :main deathrow-server.handler
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler deathrow-server.handler/app}
  :uberjar-name "deathrow-server-0.1.0-standalone.jar"
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
