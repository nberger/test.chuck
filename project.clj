(defproject com.gfredericks/test.chuck "0.1.22-SNAPSHOT"
  :description "A dumping ground of test.check utilities"
  :url "https://github.com/fredericksgary/test.chuck"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308"]
                 [instaparse "1.3.6"]]
  :deploy-repositories [["releases" :clojars]]
  :profiles {:dev {:dependencies
                   [[org.clojure/test.check "0.7.0"]]}}
  :plugins [[lein-cljsbuild "1.0.6"]]

  :clean-targets ^{:protect false} ["resources/tests.js" "resources/out-adv" "resources/out-dev"]
  :cljsbuild
  {:builds
   [{:id "dev"
     :source-paths ["src" "test"]
     :notify-command ["node" "resources/tests.js"]
     :compiler {:optimizations :none
                :output-to "resources/tests.js"
                :output-dir "resources/out-dev"
                :source-map true}}
    {:id "adv"
     :source-paths ["src" "test"]
     :notify-command ["node" "resources/tests.js"]
     :compiler {:optimizations :advanced
                :output-to "resources/tests.js"
                :output-dir "resources/out-adv"}}]}

  :aliases {"test-all"
            ^{:doc "Runs tests on multiple JVMs; profiles java-7
                    and java-8 should be defined outside this project."}
            ["do"
             "clean,"
             "with-profile" "+java-7" "test,"
             "clean,"
             "with-profile" "+java-8" "test"]})
