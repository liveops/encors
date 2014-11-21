(ns com.unbounce.encors.core-test
  (:require [clojure.test :refer :all]
            [com.unbounce.encors.types :refer [map->CorsPolicy]]
            [com.unbounce.encors.core :refer :all]))

(def default-cors-options
  {:allowed-origins nil
   :allowed-methods #{}
   :exposed-headers nil
   :request-headers #{}
   :max-age nil
   :allow-credentials? false
   :origin-varies? true
   :require-origin? true
   :ignore-failures? false})

(deftest cors-common-headers-test
  (testing "when origin is not present when origin-varies? is true"
    (let [policy (map->CorsPolicy default-cors-options)]
      (is (= (cors-common-headers nil policy)
             {"Access-Control-Allow-Origin" "*"
              "Vary" "Origin"}))))

  (testing "when origin is not present and origin-varies? is false"
    (let [policy (map->CorsPolicy (merge default-cors-options
                                         {:origin-varies? false}))]
      (is (= (cors-common-headers nil policy)
             {"Access-Control-Allow-Origin" "*"}))))

  (testing "when origin is present and allow credentials is true"
    (let [origin "foobar.com"
          policy (map->CorsPolicy (merge default-cors-options {:allowed-origins #{origin}
                                                       :allow-credentials? true}))]
      (is (= (cors-common-headers origin policy)
             {"Access-Control-Allow-Origin" origin
              "Access-Control-Allow-Credentials" "true"}))))

  (testing "when origin is present and allow credentials is false"
    (let [origin "foobar.com"
          policy (map->CorsPolicy (merge default-cors-options
                                         {:allowed-origins #{origin}}))]
      (is (= (cors-common-headers origin policy)
             {"Access-Control-Allow-Origin" origin})))))

(deftest cors-preflight-check-max-age-test
  (testing "when max-age is present"
    (let [max-age "365"
          policy (map->CorsPolicy (merge default-cors-options
                                         {:max-age max-age}))]
      (is (= (cors-preflight-check-max-age {:headers {}} policy)
             [:right {"Access-Control-Max-Age" max-age}]))))
  (testing "when max-age is not present"
    (let [policy (map->CorsPolicy default-cors-options)]
      (is (= (cors-preflight-check-max-age {:headers {}} policy))
          [:right {}]))))
