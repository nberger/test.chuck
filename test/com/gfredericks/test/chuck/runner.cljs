(ns com.gfredericks.test.chuck.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [com.gfredericks.test.chuck.generators-test]
              [com.gfredericks.test.chuck.properties-test]
              [com.gfredericks.test.chuck.clojure-test-test]
              [com.gfredericks.test.chuck.exception-handling-test]))

(doo-tests 'com.gfredericks.test.chuck.generators-test
           'com.gfredericks.test.chuck.properties-test
           'com.gfredericks.test.chuck.clojure-test-test
           'com.gfredericks.test.chuck.exception-handling-test)
