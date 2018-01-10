(ns disclojure.core
  "The main namespace for Disclojure."
  (:require
    [disclojure.gateway :as gw]))

(declare dispatch event-aliases)

(defstruct Client :token :listeners)
(defstruct Event :type :data :client)
(defstruct Listener :event :calls)

(defn create-client
  "Creates a new Disclojure client."
  [token]
  (atom (struct Client token [])))

(defn run
  "Connects the client to Discord.
   Keep in mind that this function is blocking."
  [client]
  (gw/connect (@client :token) false
    { :dispatch (partial dispatch client) }))

(defn on
  "Registers an event listener for the given client."
  [client event f]
  (swap! client assoc
    :listeners (conj (@client :listeners) (struct Listener event f)))
  client)

(defn dispatch [client type data]
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
