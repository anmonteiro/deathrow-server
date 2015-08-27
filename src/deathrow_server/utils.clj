(ns deathrow-server.utils
  (:require [compojure.route :as route]
            [ring.util.response :refer :all]))

(defn wrap-content-type-json
  "Middleware that converts responses to have the
  Content-Type: application/json; charset:utf8 header"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (nil? (get (:headers response) "Content-Type"))
        (-> response
            (header "Content-Type" "application/json; charset=utf-8"))
        response))))


(defn get-random-int
  [min max]
  (+ (rand-int (- (inc max) min)) min))


(defn return-404
  ([]
   (route/not-found "Page not found."))
  ([param]
   (route/not-found (str "Param not found: " param))))

(defn set-var!
  [v new-value]
  (alter-var-root v (constantly new-value)))