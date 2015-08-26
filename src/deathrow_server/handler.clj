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
(def page-size 20)

(def db-user (atom nil))
(def db-pw (atom nil))
(def mongo-uri (atom nil))

(def conn (atom nil))


(defn get-all-offenders
  "Return offenders using pagination. We don't
  need to test for error in numbers, since our routing
  already does that, e.g. no need for try/catch."
  ([] (get-all-offenders 1))
  ([page-num]
   (let
     ;; TODO: does it make sense to use read-string here?
     [page-num (Integer. page-num)
      offenders
      (mongo/fetch
        :offenders
        :sort {
               :executionNo -1
               }
        :skip (* (dec page-num) page-size)
        :limit page-size)]
     (response {
                :data offenders
                :paging {
                         :prev (dec page-num)
                         :next (inc page-num)}}))))


(defn get-random-statement
  "Return a random offender's information"
  []
  (redirect (str (utils/get-random-int 1 515))))


(defn get-offender-by-id
  "Return an offender given its executionNo"
  [id]
  (try
    (let
      [offender
       (mongo/fetch-one
         :offenders
         :where {
                 :executionNo (Integer/parseInt id)
                 })]
      ;; TODO: consider using if-let, but only use it if fetch-one returns nil
      ;; upon not finding the document
      (if (empty? offender)
        (utils/return-404 id)
        (response offender)))
    (catch NumberFormatException e
      (utils/return-404 id))))


(defroutes offenders-routes
  (GET "/" [] (get-all-offenders))
  (context "/page" []
           (GET "/" [] (get-all-offenders))
           (GET ["/:num", :num #"[1-9][0-9]*$"] [num] (get-all-offenders num))
           (route/not-found "Not Found"))
  (GET "/random" [] (get-random-statement))
  (GET "/:id" [id] (get-offender-by-id id))
  (route/not-found "Not Found"))


(defroutes app-routes
  (context "/offenders" [] offenders-routes)
  (route/not-found "Not Found"))


(def app
  (do
    (->
      (handler/api app-routes)
      (middleware/wrap-json-response)
      (utils/wrap-content-type-json)
      (wrap-cors :access-control-allow-origin #".*"
                 :access-control-allow-methods [:get]
                 :access-control-allow-headers ["Origin" "X-Requested-With"
                                                "Content-Type" "Accept"
                                                "Cache-Control" "Accept-Encoding"]))))

(defn- init-config-vars
  []
  (reset! db-user (System/getenv "DB_USER"))
  (reset! db-pw (System/getenv "DB_PW"))
  (reset! mongo-uri (System/getenv "MONGOLAB_URI"))
  (reset! conn
    (mongo/make-connection
      (str "mongodb://"
            @db-user
            ":"
            @db-pw
            "@"
            @mongo-uri))))

(defn -main
  [& [port]]
  (let [port (Integer. (or port
                           (System/getenv "PORT")
                           3000))]
    (init-config-vars)
    (mongo/set-connection! @conn)
    (jetty/run-jetty #'app {:port  port
                            :join? false})))
