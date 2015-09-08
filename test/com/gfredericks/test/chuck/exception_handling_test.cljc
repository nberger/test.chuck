(ns com.gfredericks.test.chuck.exception-handling-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :as test :refer [test-var test-vars *current-env*]
                      :refer-macros [is testing deftest]])
            [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.clojure-test #?(:clj :refer :cljs :refer-macros) [checking]]))

(deftest this-test-should-crash-and-be-caught
  (checking "you can divide four by numbers" 100 [i gen/pos-int]
    ;; going for uncaught-error-not-in-assertion here
    (let [n #?(:clj  (/ 4 i)
               :cljs (throw (js/Error. "Oops!")))]
      (is n))))

(defn capture-test-var [v]
  (with-out-str (test-var v)))

(deftest exception-detection-test
  (let [test-results
        #?(:clj
           (binding [; need to keep the failure of this-test-should-crash-and-be-caught
                     ; from affecting the clojure.test.check test run
                     *report-counters* (ref *initial-report-counters*)]
             (capture-test-var #'this-test-should-crash-and-be-caught)
             @*report-counters*)

           :cljs
           (binding [*current-env* (test/empty-env)]
             (capture-test-var #'this-test-should-crash-and-be-caught)
             (:report-counters *current-env*)))]
    ;; should be reported as an error, but it's being reported as :fail :/
    (is (= {:pass 0
            :fail 1
            :error 0}
           (select-keys test-results [:pass :fail :error])))))

(deftest this-test-should-fail
  (checking "int equals 2" 100 [i gen/pos-int]
    (is (= 2 i))))

(deftest failure-detection-test
  (let [[test-results out]
        #?(:clj
           (binding [; need to keep the failure of this-test-should-fail from
                     ; affecting the clojure.test.check test run
                     *report-counters* (ref *initial-report-counters*)
                     *test-out* (java.io.StringWriter.)]
             (test-var #'this-test-should-fail)
             [@*report-counters* (str *test-out*)])

           :cljs
           (binding [*current-env* (test/empty-env)]
             (capture-test-var #'this-test-should-fail)
             [(:report-counters *current-env*) nil]))]
    #?(:clj (is (not (re-find #"not-falsey-or-exception" out))))
    (is (= {:pass 1 ; TODO: why 1? Not sure, but that's what's being reported
            :fail 1
            :error 0}
           (select-keys test-results [:pass :fail :error])))))

(defn test-ns-hook []
  (test-vars [#'exception-detection-test
              #'failure-detection-test]))
