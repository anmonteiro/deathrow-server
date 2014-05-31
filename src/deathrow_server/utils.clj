(ns deathrow-server.utils
	(:use ring.util.response))

(defn wrap-content-type-json
	"Middleware that converts responses to have the
	Content-Type: application/json; charset:utf8 header"
	[handler]
	(fn [request]
		(let [response (handler request)]
			(if (= (get (:headers response) "Content-Type") nil)
				(-> response
					(header "Content-Type" "application/json; charset=utf-8"))
				response))))

(defn get-random-int
	[min max]
	(+ (rand-int (- (inc max) min)) min))

