(ns deathrow-server.handler
  (:use compojure.core)
  (:use cheshire.core)
  (:use ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [somnium.congomongo :as mongo]))

(def conn
  (mongo/make-connection "deathrow"
                     :host "127.0.0.1"
                     :port 27017))


(defn get-random-int
	[min max]
	(+ (rand-int (- (inc max) min)) min))


(defn get-random-statement
	"Return a random offender's information"
	[]
	(-> (response (let [offender (mongo/fetch-one :offenders
						:where {
							:executionNo (get-random-int 1 515)
						}
						:as :json)]
		offender))
		(header "Content-Type" "application/json; charset=utf-8")))


(defroutes app-routes
  (GET "/random" [] (get-random-statement))
  (route/not-found "Not Found"))

(mongo/set-connection! conn)

(def app
  (middleware/wrap-json-response (handler/api app-routes)))
