(defproject com.tripl3dogdare/disclojure "Indev"
  :description "A Discord API wrapper for Clojure."
  :url "https://github.com/tripl3dogdare/disclojure"
  :license {:name "MIT License"
            :url "https://spdx.org/licenses/MIT.html"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [org.clojure/data.json "0.2.6"]
    [org.clojure/core.async "0.3.465"]
    [http.async.client "1.2.0"]]
  :resource-paths ["resources/slf4j-nop-1.7.25.jar"]
  :plugins [[lein-codox "0.10.3"]]
  :codox {
    :namespaces [#"^disclojure\."]
    :metadata {
      :doc "FIXME: Document this!"
      :doc/format :markdown }
    :output-path "docs"
    :source-uri "https://github.com/tripl3dogdare/disclojure/blob/master/{filepath}#L{line}" }
  :profiles {
    :dev {
      :source-paths ["example"]
      :main testbot }})
