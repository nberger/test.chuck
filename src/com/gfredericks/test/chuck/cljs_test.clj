(ns com.gfredericks.test.chuck.cljs-test
  (:require [com.gfredericks.test.chuck.clojure-test.impl :as impl
             :refer [pass? report-when-failing save-to-final-reports]]))

(defmacro capture-reports [& body]
  `(let [reports# (atom [])]
     (binding [impl/*chuck-captured-reports* reports#
               cljs.test/*current-env* (cljs.test/empty-env :com.gfredericks.test.chuck.clojure-test.impl/chuck-capture)]
       ~@body)
     @reports#))

(defmacro checking
  "A macro intended to replace the testing macro in clojure.test with a
  generative form. To make (testing \"doubling\" (is (= (* 2 2) (+ 2 2))))
  generative, you simply have to change it to
  (checking \"doubling\" 100 [x gen/int] (is (= (* 2 x) (+ x x)))).
  For more details on this code, see http://blog.colinwilliams.name/blog/2015/01/26/alternative-clojure-dot-test-integration-with-test-dot-check/"
  [name tests bindings & body]
  `(do
     (cljs.test/testing ~name
     (let [final-reports# (atom [])]
       (report-when-failing
         (cljs.test.check/quick-check
           ~tests
           (cljs.test.check.properties/for-all ~bindings
             (let [reports# (capture-reports ~@body)]
               (swap! final-reports# save-to-final-reports reports#)
               (pass? reports#)))))
       (doseq [r# @final-reports#]
         (cljs.test/report r#))))))

(defmacro for-all
  "An alternative to clojure.test.check.properties/for-all that uses
  clojure.test-style assertions (i.e., clojure.test/is) rather than
  the truthiness of the body expression."
  [bindings & body]
  `(cljs.test.check.properties/for-all
     ~bindings
     (pass? (capture-reports ~@body))))
