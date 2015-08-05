(ns com.gfredericks.test.chuck.clojure-test.impl
  (:require #?(:clj  [clojure.test :refer [is]]
               :cljs [cljs.test :refer-macros [is]])))

;; copied from clojure.test.check, which privatized the function in
;; recent versions.
;;
;; I think there might be plans for test.check to abstract this logic
;; into a protocol or something, so I'm not too bothered by the
;; copypasta for now.
(defn ^:private not-falsey-or-exception?
  [value]
  (and value (not (instance? #?(:clj  Throwable
                                :cljs js/Error)
                             value))))

(defn report-when-failing [result]
  (is (not-falsey-or-exception? (:result result)) result))

(defn pass? [reports]
  (every? #(= (:type %) :pass) reports))

(defn report-needed? [reports final-reports]
  (or (not (pass? reports)) (empty? final-reports)))

(defn save-to-final-reports [final-reports reports]
  (if (report-needed? reports final-reports)
    reports
    final-reports))
