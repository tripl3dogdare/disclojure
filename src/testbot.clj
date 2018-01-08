(ns testbot
  (:require
    [disclojure.gateway :as gw]
    :reload-all))

(defn -main []
  (gw/connect (slurp ".auth")))
