(ns testbot
  "An example bot made with Disclojure."
  (:require
    [disclojure.core :refer :all]
    :reload-all))

(defn -main []
  (-> (slurp ".auth")
      create-client
      (on :ready #(-> % :data clojure.pprint/pprint))
      (on :message-create #(-> % :data clojure.pprint/pprint))
      (on :message-create #(-> % :data :content println))
      run))
