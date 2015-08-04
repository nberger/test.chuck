(ns com.gfredericks.test.chuck.properties
  "Alternative to clojure.test.check.properties."
  (:require [com.gfredericks.test.chuck.properties.impl :refer [for-bindings]]
            [clojure.test.check.properties :as prop]
            [com.gfredericks.test.chuck.generators :as gen']))

(defmacro for-all
  "Alternative version of clojure.test.check.properties/for-all where
  the binding forms are interpreted as per
  com.gfredericks.test.chuck.generators/for."
  [bindings expr]
  (let [bound-names (for-bindings bindings)
        quoted-names (map #(list 'quote %) bound-names)]
    `(prop/for-all [{:syms [~@bound-names]}
                    (gen'/for ~bindings
                      (with-meta
                        ~(zipmap quoted-names bound-names)
                        {::for-all-bindings-map true}))]
       ~expr)))
