(ns disclojure.gateway
  "The main gateway implementation for Disclojure."
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
(def gateway-url
  "Retrieves the URL that should be used to connect to the gateway."
  (str
    (get (with-open [client (http/create-client)]
      (let [res (http/GET client gateway-endp)]
        (-> res http/await http/string json/read-str))) "url")
    "?v=6&encoding=json"))

(defn on-receive
  "Called on receiving a message from the websocket.
   Handles:
     - Event sequence tracking
     - Dispatching events to the function given in the session's :dispatch key
     - Setting up and maintaining heartbeats
     - Receiving and processing reconnect and invalid session messages."
  [session ws msg]
  (let
    [ event (json/read-str msg :key-fn keyword)
      op (gate-ops (event :op))
      seq (event :s)
      data (event :d)
      type (keyword (event :t))
      sset (partial sset session)]
    (if (some? seq) (sset :seq seq))
    (case op
      :dispatch (do
        (if (= type :READY) (sset :sid (data :session_id)))
        ((@session :dispatch) type data))
      :heartbeat (do
        (future-cancel (@session :heartbeat-timer))
        (sset :heartbeat-timer (set-heartbeat session true)))

      :hello (let [hbi (data :heartbeat_interval)]
        (println "Connected.")
        (sset :heartbeat-int hbi)
        (sset :heartbeat-timer (set-heartbeat session))
        (ws-send (@session :socket)
          (if (@session :resume?) (resume session) (identify session))))
      :ack (sset :lastack true)

      :reconnect (reconnect session)
      :invalid-session (reconnect session data))))

(defn connect
  "Creates a new session and connects to the gateway."
  ([token] (connect token false))
  ([token resume?]
    (connect token resume? { :dispatch (fn [& _] ()) }))
  ([token resume? sdefs]
    (let
      [ session (atom (merge sdefs
          { :token token
            :lastack true
            :resume? resume?
            :connected? true }))]
      (with-open [client (http/create-client)]
        (let
          [ ws (http/websocket
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
          (while (@session :connected?) ())))
      session)))

(defn disconnect [session]
  "Disconnects the given session."
  (sset session :connected? false)
  (http/close (@session :socket))
  (future-cancel (@session :heartbeat-timer)))

(defn reconnect
  "Attempts to reconnect the given session.
   Attempts to resume by default, otherwise reidentifies from scratch."
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
  "Creates a timer to send heartbeats at the proper intervals.
   Checks for acks and attempts to reconnect if the last ack was not recieved."
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
  "A map from raw numeric opcodes to their keyword equivalents."
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
(def get-op
  "A map from keyword opcodes to their raw numeric equivalents."
  (map-invert gate-ops))

(defn- packet
  "Wraps the given opcode and data in standard packet structure."
  [op d]
  {:op (get-op op) :d d})
(defn- identify
  "Creates a new identify packet for the given session."
  [session]
  (packet :identify {
    :token (@session :token)
    :properties {
      :os (System/getProperty "os.name")
      :device "disclojure"
      :browser "disclojure" }
    :compress false }))
(defn- resume
  "Creates a new resume packet for the given session."
  [session]
  (packet :resume {
    :token (@session :token)
    :session_id (@session :sid)
    :seq (@session :seq) }))

(defn set-interval
  "Utility function for running a function at set intervals.
   If now? is true, the function will be run immediately the first time."
  ([callback ms] (set-interval callback ms false))
  ([callback ms now?]
    (future (while true (do
      (if (not now?) (Thread/sleep ms))
      (callback)
      (if now? (Thread/sleep ms)))))))
(defn ws-send
  "Converts the given message to JSON and sends it over the given socket."
  [socket msg]
  (http/send socket :text (json/write-str msg)))
(defn sset
  "Utility function for updating fields on the given session."
  [session & args]
  (apply (partial swap! session assoc) args))
