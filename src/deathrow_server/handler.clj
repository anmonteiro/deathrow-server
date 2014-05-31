(ns deathrow-server.handler
	(:use compojure.core)
	(:use cheshire.core)
	(:use ring.util.response)
	(:require [compojure.handler :as handler]
		[compojure.route :as route]
		[ring.middleware.json :as middleware]
		[somnium.congomongo :as mongo]
		[ring.middleware.cors :refer [wrap-cors]]
		[deathrow-server.utils :as utils]))

(def conn
	(mongo/make-connection "deathrow"
		:host "127.0.0.1"
		:port 27017))

(mongo/set-connection! conn)


(defn get-random-statement
	"Return a random offender's information"
	[]
	(response
		(mongo/fetch-one
			:offenders
			:where {
				:executionNo (utils/get-random-int 1 515)
			})))


(defn get-offender-by-id
	"Return an offender given its _id"
	[id]
	(try
		(let
			[offender
				(mongo/fetch-one
					:offenders
					:where {
						:_id (Integer/parseInt id)
					})]
			(if (empty? offender)
				(utils/return-404 id)
				(response offender)))
	(catch NumberFormatException e
		(utils/return-404 id))))


(defroutes offenders-routes
	(GET "/random" [] (get-random-statement))
	(GET "/:id" [id] (get-offender-by-id id))
	(route/not-found "Not Found"))


(defroutes app-routes
	(context "/offenders" [] offenders-routes)
	(route/not-found "Not Found"))


(def app
	(->
		(handler/api app-routes)
		(middleware/wrap-json-response)
		(utils/wrap-content-type-json)
		(wrap-cors :access-control-allow-origin #"http://anmonteiro.github.io"
			:access-control-allow-methods [:get])))
