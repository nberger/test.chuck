(ns com.gfredericks.test.chuck.clojure-test-output-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.test-utils :refer [capture-report-counters-and-out]]
            [com.gfredericks.test.chuck.clojure-test :refer [checking for-all]]))

(deftest a-failing-test
  (checking "all ints lt 5" 100
             [i gen/int]
             (is (< i 5))))

(deftest failure-output-test
  (let [[test-results out] (capture-report-counters-and-out #'a-failing-test)]
    (is (re-find #"expected: \(< i 5\)" out))
    (is (re-find #"actual: \(not \(< \d 5\)" out))
    (is (re-find #"\{.*:result false.*\}" out))
    (let [tc-report (second (re-find #"(\{:result false.*\})" out))]
      (is tc-report)
      (when-let [tc-report (and tc-report (read-string tc-report))]
        (is (not (:result tc-report)))
        (is (= [5] (get-in tc-report [:shrunk :smallest])))))))

(defn test-ns-hook []
  (test-vars [#'failure-output-test]))
