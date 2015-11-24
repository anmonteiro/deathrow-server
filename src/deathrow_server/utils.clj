(ns deathrow-server.utils
  (:require [compojure.route :as route]))

(defn get-random-int
  ([max] (get-random-int 1 max))
  ([min max]
   (+ (rand-int (- (inc max) min)) min)))

(defn return-404
  ([]
   (route/not-found "Page not found."))
  ([param]
   (route/not-found (str "Param not found: " param))))

(defn set-var!
  [v new-value]
  (alter-var-root v (constantly new-value)))
