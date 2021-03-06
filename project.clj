(defproject deathrow-server "1.1.0-SNAPSHOT"
  :description "Random last statements by executed offenders"
  :url "https://github.com/anmonteiro/deathrow-server"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-alpha10"]
                 [compojure "1.5.1" :exclusions [[ring/ring-core] [commons-codec]]]
                 [com.cognitect/transit-clj "0.8.285"]
                 [congomongo "0.5.0"]
                 [org.omcljs/om "1.0.0-alpha40"]
                 [ring "1.6.0-beta4"]
                 [ring-cors "0.1.8"]
                 [ring-transit "0.1.6"]]
  :main deathrow-server.handler
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler deathrow-server.handler/app
         :init deathrow-server.handler/init!
         :destroy deathrow-server.handler/destroy!}
  :uberjar-name "deathrow-server-1.0.0-standalone.jar"
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "3.0-alpha-1"]
                                  [ring/ring-mock "0.3.0"]]}
             :uberjar {:aot :all}})
