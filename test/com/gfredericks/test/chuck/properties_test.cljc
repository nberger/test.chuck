(ns com.gfredericks.test.chuck.properties-test
  #?(:cljs (:require-macros [com.gfredericks.test.chuck.properties :as prop']
                            [com.gfredericks.test.chuck.generators :as gen']))
  #?(:clj  (:require [clojure.test :refer :all]
                     [clojure.test.check :as t.c]
                     [clojure.test.check.generators :as gen]
                     [clojure.test.check.clojure-test :refer [defspec]]
                     [com.gfredericks.test.chuck.properties :as prop'])
     :cljs (:require [cljs.test :refer-macros [run-tests deftest is]]
                     [cljs.test.check :as t.c]
                     [cljs.test.check.generators :as gen]
                     [cljs.test.check.properties :include-macros true]
                     [cljs.test.check.cljs-test :refer-macros [defspec]])))

(deftest it-handles-exceptions-correctly
  (is
   (instance? #?(:clj Throwable :cljs js/Error)
              (:result
               (t.c/quick-check 100
                 #?(
                 :clj
                 (prop'/for-all [x gen/int]
                   (/ 4 0))

                 :cljs 
                 (prop'/for-all-cljs [x gen/int]
                   (js/Error. "Oops"))))))))

(deftest reported-args-test
  (let [p (#?(:clj  prop'/for-all
              :cljs prop'/for-all-cljs) [x gen/nat]
              (not (<= 0 x 10)))
        {:keys [fail]} (t.c/quick-check 1000 p)]
    (is (= 1 (count fail)))
    (let [[m] fail]
      (is (= ['x] (keys m)))
      (is (<= 0 (get m 'x) 10)))))

(defspec for-all-destructured-args-work-correctly 10
  (#?(:clj prop'/for-all :cljs prop'/for-all-cljs) [[a b] (gen/tuple gen/int gen/int)]
                 (+ a b)))






#?(:cljs
(defn js-print [& args]
  (if (js* "typeof console != 'undefined'")
    (.log js/console (apply str args))
    (js/print (apply str args)))))

#?(:cljs (set! *print-fn* js-print))

#?(:cljs (run-tests))
