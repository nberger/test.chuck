(ns com.gfredericks.test.chuck.clojure-test-test
  #?(:clj  (:require [clojure.test :refer :all]
                     [clojure.test.check :refer [quick-check]]
                     [clojure.test.check.generators :as gen]
                     [com.gfredericks.test.chuck.clojure-test :refer [checking for-all]])
     :cljs (:require [cljs.test :refer-macros [deftest is run-tests]]
                     [cljs.test.check :refer [quick-check]]
                     [com.gfredericks.test.chuck.cljs-test :refer-macros [checking for-all]]
                     [cljs.test.check.generators :as gen])))

(deftest integer-facts
  (checking "positive" 100 [i gen/s-pos-int]
    (is (> i 0)))

  (checking "negative" 100 [i gen/s-neg-int]
    (is (< i 0))))

(deftest counter
  (checking "increasing" 100 [i gen/s-pos-int]
    (let [c (atom i)]
      (swap! c inc)
      (is (= @c (inc i)))
      (swap! c inc)
      (is (> @c 0)))))

(deftest for-all-test
  (let [passing-prop (for-all [x gen/s-pos-int]
                       (is (< x (+ x x))))]
    (is (true? (:result (quick-check 20 passing-prop)))))
  (let [failing-prop (for-all [x gen/s-pos-int]
                       (is true)
                       ;; sticking a failing assertion in between two
                       ;; passing ones
                       (is (zero? x))
                       (is (= x x)))]
    (is (not (:result (quick-check 20 failing-prop))))))


#?(:cljs
(defn js-print [& args]
  (if (js* "typeof console != 'undefined'")
    (.log js/console (apply str args))
    (js/print (apply str args)))))

#?(:cljs (set! *print-fn* js-print))

#?(:cljs (run-tests))
