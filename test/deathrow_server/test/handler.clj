(ns deathrow-server.test.handler
  (:require [cognitect.transit :as t]
            [clojure.test :refer [deftest testing is are use-fixtures]]
            [ring.mock.request :as mock]
            [deathrow-server.handler :as dt :refer [app conn]]
            [somnium.congomongo :as m])
  (import [java.io ByteArrayInputStream]))

(defn db-fixture [f]
  (dt/init!)
  (f)
  (dt/destroy!))

(use-fixtures :once db-fixture)

;; Helpers

(defn- response-body->clj [res]
  (t/read (t/reader (ByteArrayInputStream.
                     (.getBytes (:body res)))
                    :json)))

;; Tests

(deftest test-app
  (testing ""
    (let [response (app (mock/request :get "/offenders/414"))]
      (is (= (re-matches #".*?application.*?"
                         (get (:headers response) "Content-Type"))
             "application/transit+json; charset=utf-8"))))
  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))
  (testing "responses wrapped in :data"
    (let [response-o (app (mock/request :get "/offenders/414"))
          body-o (response-body->clj response-o)
          response-os (app (mock/request :get "/offenders"))
          body-os (response-body->clj response-os)]
      (are [body] (contains? body :data)
        body-o
        body-os))))

(deftest test-offenders
  (testing "random route"
    (let [response (app (mock/request :get "/offenders/random"))]
      (is (= (:status response) 302))))
  (testing "get all offenders"
    (let [response (app (mock/request :get "/offenders/"))]
      (is (= (:status response) 200))
      (let [body (response-body->clj response)
            total (count (:data body))]
        (is (= total 20)))))
  (testing "pagination"
    (let [response (app (mock/request :get "/offenders/page/4"))]
      (let [body (response-body->clj response)
            total (count (:data body))]
        (is (= total 20))
        (is (= (count (:paging body)) 2)))))
  (testing "get offender by execution number"
    (let [response (app (mock/request :get "/offenders/414"))]
      (let [body (response-body->clj response)
            id (get-in body [:data :_id])]
        (is (= id 999313))))))
