(ns com.gfredericks.test.chuck.cljs-test
  (:require [com.gfredericks.test.chuck.clojure-test.impl
             :refer [pass? report-when-failing save-to-final-reports]]
            #?(:cljs [cljs.test.check.properties :include-macros true])))

(def ^:dynamic *chuck-captured-reports*)

#?(:cljs
(defmethod cljs.test/report [::chuck-capture :fail]
  [m]
  (swap! *chuck-captured-reports* conj m)))

#?(:cljs
(defmethod cljs.test/report [::chuck-capture :pass]
  [m]
  (swap! *chuck-captured-reports* conj m)))

#?(:clj
(defmacro capture-reports [& body]
  `(let [reports# (atom [])]
     (binding [*chuck-captured-reports* reports#
               cljs.test/*current-env* (cljs.test/empty-env ::chuck-capture)]
       ~@body)
     @reports#)))

#?(:clj
(defmacro checking
  "A macro intended to replace the testing macro in clojure.test with a
  generative form. To make (testing \"doubling\" (is (= (* 2 2) (+ 2 2))))
  generative, you simply have to change it to
  (checking \"doubling\" 100 [x gen/int] (is (= (* 2 x) (+ x x)))).
  For more details on this code, see http://blog.colinwilliams.name/blog/2015/01/26/alternative-clojure-dot-test-integration-with-test-dot-check/"
  [name tests bindings & body]
  `(cljs.test/testing ~name
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

#?(:clj
(defmacro for-all
  "An alternative to clojure.test.check.properties/for-all that uses
  clojure.test-style assertions (i.e., clojure.test/is) rather than
  the truthiness of the body expression."
  [bindings & body]
  `(cljs.test.check.properties/for-all
     ~bindings
     (pass? (capture-reports ~@body)))))
