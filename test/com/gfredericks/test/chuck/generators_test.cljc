(ns com.gfredericks.test.chuck.generators-test
  (:require #?@(:clj  [[clojure.test.check.clojure-test :refer [defspec]]
                       [clojure.test.check.generators :as gen]
                       [clojure.test.check.properties :as prop]
                       [com.gfredericks.test.chuck.generators :as gen']]
                :cljs [[cljs.test.check.cljs-test :refer-macros [defspec]]
                       [cljs.test.check]
                       [cljs.test.check.generators :as gen]
                       [cljs.test.check.properties :as prop :include-macros true]
                       [com.gfredericks.test.chuck.generators :as gen' :include-macros true]])))

(def lists-and-counts
  (gen'/for [nums (gen/vector gen/nat)
             :let [cardinality (count nums)]]
    [nums cardinality]))

(defspec for-works-correctly 100
  (prop/for-all [[nums cardinality] lists-and-counts]
    (= (count nums) cardinality)))

(def lists-with-two-of-their-elements
  (gen'/for [nums (gen/vector gen/nat)
             :let [cardinality (count nums)]
             :when (> cardinality 1)
             x (gen/elements nums)
             :let [[befores [_x & afters]] (split-with #(not= % x) nums)
                   nums-x (concat befores afters)]
             y (gen/elements nums-x)]
    [nums x y]))

(defspec complex-for-works-correctly 100
  (prop/for-all [[nums x y] lists-with-two-of-their-elements]
    (let [f (frequencies nums)]
      ;; check that both x and y are in the list
      (or (and (= x y) (> (f x) 1))
          (and (not= x y) (pos? (f x)) (pos? (f y)))))))

(def destructuring-usage
  (gen'/for [{:keys [foo]} (gen/hash-map :foo gen/nat)
             :let [unused-binding 42]
             vs (gen/vector gen/boolean foo)]
    [foo vs]))

(defspec destructuring-usage-spec 100
  (prop/for-all [[n vs] destructuring-usage]
    (= n (count vs))))

(def parallel-usage
  (gen'/for [:parallel [x gen/nat
                        y gen/boolean]]
    [x y]))

(defspec parallel-usage-spec 100
  (prop/for-all [[x y] parallel-usage]
    (and (>= x 0)
         (or (= true y) (= false y)))))

(def parallel-as-second-clause
  (gen'/for [n gen/nat
             :parallel [v1 (gen/vector gen/boolean n)
                        v2 (gen/vector gen/boolean n)]]
    [n (concat v1 v2)]))

(defspec parallel-as-second-clause-spec 100
  (prop/for-all [[n v] parallel-as-second-clause]
    (= (* 2 n) (count v))))

(defspec bounded-int-generates-bounded-ints 500
  (let [large-int (gen/choose -200000000 200000000)
        g (gen/bind (gen/tuple large-int large-int)
                    (fn [pair]
                      (let [[low high] (sort pair)]
                        (gen/tuple (gen/return low)
                                   (gen/return high)
                                   (gen'/bounded-int low high)))))]
    (prop/for-all [[low high n] g]
      (<= low n high))))

; TODO: improve the cljs tests for gen'/double
(defspec double-generates-doubles 100
  (prop/for-all [x gen'/double]
    #?(:clj  (instance? Double x)
       :cljs (= js/Number (type x)))))

(defspec subset-in-set 100
  (prop/for-all [s (gen'/subset (range 10))]
    (every? (set (range 10)) s)))

(defn subsequence?
  "Checks if xs is a subsequence of ys."
  [xs ys]
  (or (empty? xs)
      (and (seq ys)
           (= (first xs) (first ys))
           (subsequence? (rest xs) (rest ys)))
      (and (seq ys)
           (subsequence? xs (rest ys)))))

(def subsequence-gen
  (gen'/for [ys (gen/list gen/nat)
             xs (gen'/subsequence ys)]
    [xs ys]))

(defspec subsequence-spec 100
  (prop/for-all [[xs ys] subsequence-gen]
    (subsequence? xs ys)))

(def sub-map-gen
  (gen'/for [m (gen/map gen/string-alphanumeric gen/nat)
             sm (gen'/sub-map m)]
    [m sm]))

(defspec sub-map-spec 100
  (prop/for-all [[m sm] sub-map-gen]
    (every? #(= (find m (key %))
                %)
            sm)))
