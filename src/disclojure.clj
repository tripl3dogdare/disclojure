(ns disclojure
  (:require
    [disclojure.gateway :as gw]
    [http.async.client :as http]))

(def bot-defaults
  { :listeners []
    :gateway nil })

(defn new [token]
  (atom (merge bot-defaults { :token token })))

(defn run [bot]
  (swap! bot assoc
    :gateway (gw/connect (@bot :token) false { :dispatch println })))
