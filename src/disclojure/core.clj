(ns disclojure.core
  "The main namespace for Disclojure."
  (:require
    [disclojure.gateway :as gw]
    [disclojure.cache :as cache]))

(declare dispatch event-aliases on)

(defstruct
  ^{:doc "[Struct] An instance of the client used to connect to Discord."}
  Client :token :listeners :cache)
(defstruct
  ^{:doc "[Struct] An individual event received from the gateway."}
  Event :type :data :client)
(defstruct
  ^{:doc "[Struct] An event listener. (You should never need to create these yourself! Use [[on]] instead.)"}
  Listener :event :calls)

(defn create-client
  "Creates a new [[Client]].

  Parameters:

  - `token` The token to connect with.
  - `cache?` If true, event handlers related to caching are automatically attached (default: `true`)."
  ([token] (create-client token true))
  ([token cache?]
    (let
      [ client (atom (struct Client token [] (cache/create-cache))) ]

      ;; Apply caching handlers
      (when cache?
        (-> client
            (on :ready (fn [{data :data}]
              (doto (@client :cache)
                    (cache/insert :user (-> data :user :id) (data :user))
                    (#(doseq [ channel (data :private_channels) ]
                      (cache/insert % :channel (channel :id) channel)))
                    (#(doseq [ guild (data :guilds) ]
                      (cache/insert % :guild (guild :id) guild))))))

            ;; Guild Events
            (on [:guild-create :guild-update] (fn [{data :data}]
              (doto (@client :cache)
                    (cache/insert :guild (data :id) data)
                    (#(doseq [ member (data :members) ]
                      (cache/insert % :user (-> member :user :id) (member :user))))
                    (#(doseq [ channel (data :channels) ]
                      (cache/insert % :channel (channel :id) channel)))
                    (#(doseq [ role (data :roles) ]
                      (cache/insert % :role (role :id) role))))))
            (on :guild-delete (fn [{data :data}]
              (cache/uncache (@client :cache) :guild (data :id))))))

      client)))

(defn run
  "Connects the client to the gateway, logging it in.
   Keep in mind that this function is blocking.

   Parameters:

   - `client` The client to connect."
  [client]
  (gw/connect (@client :token) false
    { :dispatch (partial dispatch client) }))

(defn on
  "Registers an event listener for the given client.

  Parameters:

  - `client` The client to add the listener to.
  - `event` The event to listen for (see [[event-aliases]]). Can also take a vector of multiple events to listen for.
  - `f` The function to call when the event is received."
  [client event f]
  (if (vector? event)
    (swap! client assoc
      :listeners (apply (partial conj (@client :listeners)) (map #(struct Listener % f) event)))
    (swap! client assoc
      :listeners (conj (@client :listeners) (struct Listener event f))))
  client)

(defn- dispatch [client type data]
  "Dispatches an event to the given client."
  (let
    [ re (keyword (.toLowerCase (.replaceAll (name type) "_" "-")))
      ev (or (event-aliases re) re)
      fl (filter #(= (% :event) ev) (@client :listeners))]
    (doseq [{f :calls} fl]
      (future (f (struct Event ev data client))))))

(def event-aliases
  "A mapping from event name aliases to their root events."
  { :member-banned :guild-ban-add
    :member-unbanned :guild-ban-remove

    :emojis-update :guild-emojis-update
    :integrations-update :guild-integrations-update

    :member-add :guild-member-add
    :member-added :guild-member-add
    :member-join :guild-member-add
    :member-joined :guild-member-add

    :member-remove :guild-member-remove
    :member-removed :guild-member-remove
    :member-leave :guild-member-remove
    :member-left :guild-member-remove

    :member-update :guild-member-update

    :role-create :guild-role-create
    :role-created :guild-role-create
    :role-add :guild-role-create
    :role-added :guild-role-create

    :role-delete :guild-role-delete
    :role-deleted :guild-role-delete
    :role-remove :guild-role-delete
    :role-removed :guild-role-delete

    :role-update :guild-role-update

    :message :message-create
    :message-created :message-create
    :message-add :message-create
    :message-added :message-create
    :message-send :message-create
    :message-sent :message-create

    :message-deleted :message-delete
    :message-remove :message-delete
    :message-removed :message-delete

    :react :message-reaction-add
    :reaction-add :message-reaction-add
    :reaction-added :message-reaction-add

    :reaction-remove :message-reaction-remove
    :reaction-removed :message-reaction-remove

    :reaction-remove-all :message-reaction-remove-all })
