(ns deathrow-server.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [deathrow-server.handler :refer :all]
            [cheshire.core :refer :all]
            [somnium.congomongo :refer :all]))

(deftest test-app
  (testing ""
    (let [response (app (request :get "/offenders/414"))]
      (is (= (re-matches #".*?application/json.*?" (get (:headers response) "Content-Type")) "application/json; charset=utf-8"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))

(deftest test-offenders
  (testing "random route"
    (let [response (app (request :get "/offenders/random"))]
      (is (= (:status response) 302))))

  (testing "get all offenders"
    (let [response (app (request :get "/offenders/"))]
      (is (= (:status response) 200))
      (let [body (parse-string (:body response) true)
            total (count (:data body))]
        (is (= total 20)))))

  (testing "pagination"
    (let [response (app (request :get "/offenders/page/4"))]
      (let [body (parse-string (:body response) true)
            total (count (:data body))]
        (is (= total 20))
        (is (= (count (:paging body)) 2)))))

  (testing "get offender by execution number"
    (let [response (app (request :get "/offenders/414"))]
      (let [id (get (parse-string (:body response) true) :_id)]
        (is (= id 999313)))))
  (close-connection conn))
