(ns deathrow-server.handler
	(:use compojure.core)
	(:use cheshire.core)
	(:use ring.util.response)
	(:require [compojure.handler :as handler]
		[compojure.route :as route]
		[ring.middleware.json :as middleware]
		[somnium.congomongo :as mongo]
		[ring.middleware.cors :refer [wrap-cors]]
		[deathrow-server.utils :as utils]
		[ring.adapter.jetty :as jetty])
	(:gen-class))

;; CONSTANTS
(def PAGE-SIZE 20)

(def conn
	(mongo/make-connection "deathrow"
		:host "127.0.0.1"
		:port 27017))



(defn get-all-offenders
	"Return offenders using pagination. We don't
	need to test for error in numbers, since our routing
	already does that, e.g. o need for try/catch."
	([] (get-all-offenders 1))
	([page-num]
		(let
			[page-num (Integer. page-num)
			offenders
				(mongo/fetch
					:offenders
					:sort {
						:executionNo -1
					}
					:skip (* (dec page-num) PAGE-SIZE)
					:limit PAGE-SIZE)]
			(response {
				:data offenders
				:paging {
					:prev (dec page-num)
					:next (inc page-num)
				}
			}))))


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
	(GET "/" [] (get-all-offenders))
	(context "/page" []
		(GET "/" [] (get-all-offenders))
		(GET "/:num" [num] (get-all-offenders num)))
	(GET "/random" [] (get-random-statement))
	(GET "/:id" [id] (get-offender-by-id id))
	(route/not-found "Not Found"))


(defroutes app-routes
	(context "/offenders" [] offenders-routes)
	(route/not-found "Not Found"))


(def app
	(do
		(mongo/set-connection! conn)
		(->
			(handler/api app-routes)
			(middleware/wrap-json-response)
			(utils/wrap-content-type-json)
			(wrap-cors :access-control-allow-origin #"http://anmonteiro.github.io"
				:access-control-allow-methods [:get]))))

(defn -main
  [& [port]]
  (let [port (Integer. (or port
                           (System/getenv "PORT")
                           3000))]
    (jetty/run-jetty #'app {:port  port
                            :join? false})))


