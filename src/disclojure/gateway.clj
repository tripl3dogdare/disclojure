(ns disclojure.gateway
  (:require
    [clojure.data.json :as json]
    [http.async.client :as http]
    [clojure.set :refer [map-invert]]))

(declare
  set-interval ws-send sset
  gate-ops get-op
  identify resume
  connect disconnect reconnect
  set-heartbeat)

(def gateway-endp "https://discordapp.com/api/v6/gateway")
(def gateway-url (str
  (get (with-open [client (http/create-client)]
    (let [res (http/GET client gateway-endp)]
      (-> res http/await http/string json/read-str))) "url")
  "?v=6&encoding=json"))

(def dispatch (fn [& _] ()))

(defn on-receive
  [session ws msg]
  (let
    [ event (json/read-str msg :key-fn keyword)
      op (gate-ops (event :op))
      seq (event :s)
      data (event :d)
      type (keyword (event :t))
      sset (partial sset session)]
    (println op type)
    (if (some? seq) (sset :seq seq))
    (case op
      :dispatch (do
        (if (= type :READY) (sset :sid (data :session_id)))
        (dispatch type data))
      :heartbeat (do
        (future-cancel (@session :heartbeat-timer))
        (sset :heartbeat-timer (set-heartbeat session true)))

      :hello (let [hbi (data :heartbeat_interval)]
        (sset :heartbeat-int hbi)
        (sset :heartbeat-timer (set-heartbeat session))
        (ws-send (@session :socket)
          (if (@session :resume?) (resume session) (identify session))))
      :ack (sset :lastack true)

      :reconnect (reconnect session)
      :invalid-session (reconnect session data))))

(defn connect
  ([token] (connect token false))
  ([token resume?] (connect token resume? {}))
  ([token resume? sdefs]
    (with-open [client (http/create-client)]
      (let
        [ session (atom (merge sdefs
            { :token token
              :lastack true
              :resume? resume?
              :connected? true }))
          ws (http/websocket
            client gateway-url
            :text (partial on-receive session)
            :error
              #(do
                (println "An error occured:" (:cause (Throwable->map %2)))
                (disconnect session))
            :close
              #(do
                (println "Connection closed:" %2 %3)
                (disconnect session)))]
        (sset session :socket ws)
        (if (not resume?) (sset session :seq 0))
        (while (@session :connected?) ())))))

(defn disconnect [session]
  (sset session :connected? false)
  (http/close (@session :socket))
  (future-cancel (@session :heartbeat-timer)))

(defn reconnect
  ([session] (reconnect session true))
  ([session resume?]
    (let
      [ token (@session :token)
        sid (@session :sid)
        seq (@session :seq)]
      (disconnect session)
      (Thread/sleep 2000)
      (if resume?
        (connect token true { :sid sid :seq seq })
        (connect token)))))

(defn set-heartbeat
  ([session] (set-heartbeat session false))
  ([session now?]
    (set-interval
      #(if (@session :lastack)
        (do
          (sset session :lastack false)
          (ws-send (@session :socket) { :op 1 :d (@session :seq) }))
        (reconnect session))
      (@session :heartbeat-int) now?)))

(def gate-ops
  { 0 :dispatch
    1 :heartbeat
    2 :identify
    3 :status-update
    4 :voice-state-update
    5 :voice-server-ping
    6 :resume
    7 :reconnect
    8 :request-guild-members
    9 :invalid-session
    10 :hello
    11 :ack })
(def get-op (map-invert gate-ops))

(defn packet [op d] {:op (get-op op) :d d})
(defn identify [session] (packet :identify {
  :token (@session :token)
  :properties {
    :os (System/getProperty "os.name")
    :device "disclojure"
    :browser "disclojure" }
  :compress false }))
(defn resume [session] (packet :resume {
  :token (@session :token)
  :session_id (@session :sid)
  :seq (@session :seq) }))

(defn set-interval
  ([callback ms] (set-interval callback ms false))
  ([callback ms now?]
    (future (while true (do
      (if (not now?) (Thread/sleep ms))
      (callback)
      (if now? (Thread/sleep ms)))))))
(defn ws-send [socket msg]
  (println msg)
  (http/send socket :text (json/write-str msg)))
(defn sset [session & args]
  (apply (partial swap! session assoc) args))
