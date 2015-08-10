(ns com.gfredericks.test.chuck.clojure-test
  #?(:clj  (:require [clojure.test.check :as tc]
                     [clojure.test :refer :all]
                     [clojure.test.check.properties :as prop]
                     [com.gfredericks.test.chuck.clojure-test.impl :refer :all])
     :cljs (:require [com.gfredericks.test.chuck.clojure-test.impl
                      :refer [pass? report-when-failing save-to-final-reports]]
                     [clojure.test.check.properties :include-macros true])))

(def ^:dynamic *chuck-captured-reports*)

#?(:cljs
(defmethod cljs.test/report [::chuck-capture :fail]
  [m]
  (swap! *chuck-captured-reports* conj m)))

#?(:cljs
(defmethod cljs.test/report [::chuck-capture :pass]
  [m]
  (swap! *chuck-captured-reports* conj m)))

(defn- cljs-env?
  "Take the &env from a macro, and tell whether we are expanding into cljs."
  [env]
  (boolean (:ns env)))

(defmacro if-cljs
  "Return then if we are generating cljs code and else for Clojure code.
   https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ"
  [then else]
  (if (cljs-env? &env) then else))

(defmacro checking
  "A macro intended to replace the testing macro in clojure.test with a
  generative form. To make (testing \"doubling\" (is (= (* 2 2) (+ 2 2))))
  generative, you simply have to change it to
  (checking \"doubling\" 100 [x gen/int] (is (= (* 2 x) (+ x x)))).

  For more details on this code, see http://blog.colinwilliams.name/blog/2015/01/26/alternative-clojure-dot-test-integration-with-test-dot-check/"
  [name tests bindings & body]
  `(if-cljs

   (cljs.test/testing ~name
     (let [final-reports# (atom [])]
       (report-when-failing
         (clojure.test.check/quick-check
           ~tests
           (clojure.test.check.properties/for-all ~bindings
             (let [reports# (atom [])]
               (binding [*chuck-captured-reports* reports#
                         cljs.test/*current-env* (cljs.test/empty-env ::chuck-capture)]
                 ~@body)
               (swap! final-reports# save-to-final-reports @reports#)
               (pass? @reports#)))))
       (doseq [r# @final-reports#]
         (cljs.test/report r#)))) 

   (testing ~name
     (let [final-reports# (atom [])]
       (report-when-failing (tc/quick-check ~tests
                              (prop/for-all ~bindings
                                (let [reports# (atom [])]
                                  (binding [report #(swap! reports# conj %)]
                                    ~@body)
                                  (swap! final-reports# save-to-final-reports @reports#)
                                  (pass? @reports#)))))
       (doseq [r# @final-reports#]
         (report r#))))))

(defmacro for-all
  "An alternative to clojure.test.check.properties/for-all that uses
  clojure.test-style assertions (i.e., clojure.test/is) rather than
  the truthiness of the body expression."
  [bindings & body]
  `(if-cljs

   (clojure.test.check.properties/for-all
     ~bindings
     (let [reports# (atom [])]
       (binding [*chuck-captured-reports* reports#
                 cljs.test/*current-env* (cljs.test/empty-env ::chuck-capture)]
         ~@body)
       (pass? @reports#)))

   (prop/for-all ~bindings
     (let [reports# (atom [])]
       (binding [report #(swap! reports# conj %)]
         ~@body)
       (pass? @reports#)))))
