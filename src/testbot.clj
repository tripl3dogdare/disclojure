(ns testbot
  (:require
    [disclojure :as dc]
    :reload-all))

(defn -main []
  (-> (slurp ".auth")
      dc/new
      dc/run))
