(ns com.gfredericks.test.chuck.test-utils
  (:require [clojure.test :refer :all]))

(defn- capture-test-var [v]
  (with-out-str (test-var v)))

(defn capture-report-counters-and-out [test]
  (binding [; need to keep the failure of the test
            ; from affecting the clojure.test.check test run
            *report-counters* (ref *initial-report-counters*)
            *test-out* (java.io.StringWriter.)]
    (capture-test-var test)
    [@*report-counters* (str *test-out*)]))

