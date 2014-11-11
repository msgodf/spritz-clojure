(defproject spritz-clojure "0.1.0-SNAPSHOT"
  :description "An implementation of the Spritz cipher in Clojure."
  :url "https://github.com/msgodf/spritz-clojure"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.0"]]
  :plugins [[cider/cider-nrepl "0.7.0"]
            [lein-midje "3.1.3"]]
  :profiles
  {:dev {:dependencies [[midje "1.6.3"]]}})
