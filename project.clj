(defproject disclojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url "https://spdx.org/licenses/MIT.html"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [org.clojure/data.json "0.2.6"]
    [org.clojure/core.async "0.3.465"]
    [http.async.client "1.2.0"]]
  :resource-paths ["resources/slf4j-nop-1.7.25.jar"]
  :main testbot)
