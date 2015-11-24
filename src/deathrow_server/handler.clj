(ns deathrow-server.handler
  (:require [deathrow-server.utils :as utils]
            [cognitect.transit :as transit]
            [compojure.core :refer [context defroutes GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.transit :as middleware]
            [ring.util.response :refer [redirect response]]
            [somnium.congomongo :as m])
  (:gen-class))

;; CONSTANTS
(def page-size 20)
(def coll :offenders)

;; Environment variables (bound later)
(def db-user)
(def db-pw)
(def mongo-uri)

;; DB Connection
(def conn (atom nil))

(defn- wrap-response [res & more]
  (-> (apply merge {:data res} more)
    (response)))

(defn get-all-offenders
  "Return offenders using pagination. We don't
  need to test for error in numbers, since our routing
  already does that, e.g. no need for try/catch."
  ([] (get-all-offenders 1))
  ([page-num]
   (let [page-num (Integer. page-num)
         offenders (m/fetch coll
                            :sort {:executionNo -1}
                            :skip (* (dec page-num) page-size)
                            :limit page-size)]
     (wrap-response offenders
                    {:paging {:prev (dec page-num)
                              :next (inc page-num)}}))))

(defn get-random-statement
  "Return a random offender's information"
  []
  (let [count (m/fetch-count coll)]
    (redirect (str (utils/get-random-int count)))))

(defn get-offender-by-id
  "Return an offender given its executionNo"
  [id]
  (try
    (if-let [offender (m/fetch-one coll
                                   :where {:executionNo (Integer. id)})]
      (wrap-response offender)
      (utils/return-404 id))
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

(defn- init-config-vars []
  (utils/set-var! (var db-user) (System/getenv "DB_USER"))
  (utils/set-var! (var db-pw) (System/getenv "DB_PW"))
  (utils/set-var! (var mongo-uri) (System/getenv "MONGOLAB_URI"))
  (reset! conn
          (m/make-connection
            (str "mongodb://" db-user ":" db-pw "@" mongo-uri))))

(defn init! []
  (init-config-vars)
  (m/set-connection! @conn))

(defn destroy! []
  (m/close-connection @conn)
  (reset! conn nil))

(def app
  (->
    (handler/api app-routes)
    ;; this should be called before any middleware that sets the content type
    (middleware/wrap-transit-response {:encoding :json :opts {}})
    (wrap-cors :access-control-allow-origin #".*localhost.*|.*anmonteiro.com.*"
               :access-control-allow-methods [:get]
               :access-control-allow-headers ["Origin" "X-Requested-With"
                                              "Content-Type" "Accept"
                                              "Cache-Control" "Accept-Encoding"])))

(defn -main
  [& [port]]
  (let [port (Integer. (or port
                           (System/getenv "PORT")
                           3000))]
    (init!)
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(destroy!)))
    (jetty/run-jetty #'app {:port  port
                            :join? false})))
