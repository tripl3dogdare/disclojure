(ns disclojure.rest
  "REST endpoints and request support for Disclojure.

   The majority of the methods in this namespace are direct mappings to Discord endpoints. They take a
    [[Client]] instance to make the request from, any IDs or other information they need to fill in the endpoint path,
    and finally a map containing the data to send (either querystring keys/values or JSON-formatted body data, depending;
    the endpoint methods will convert the map correctly as necessary). All endpoint methods take a data parameter, even
    if the endpoint doesn't need any data; in that case, just pass in an empty map (this is due to use of a macro
    to greatly streamline the definition process).

   Endpoints are listed here in alphabetical order. In the source, they are defined top-to-bottom based on the
    API documentation; starting with the first endpoint on the first page and ending with the last endpoint on the
    last page. This is to streamline editing and management of the endpoints in relation to the documentation.

   Discord API Documentation: https://discordapp.com/developers/docs/reference

   **Only use this directly if you really need to! Prefer the methods exposed by [[disclojure.core]] whenever possible!**"
  (:require
    [disclojure.info :refer :all]
    [clojure.data.json :as json]
    [http.async.client :as http]))

(def ^:private api-url "https://discordapp.com/api/v6")

(defn- headers
  "Gets the headers for the given client."
  [client]
  { :Authorization (format "Bot %s" (.trim (@client :token)))
    :User-Agent (format "DiscordBot (%s, %s)" URL VERSION)
    :Content-Type "application/json" })

(defn- request
  "Sends a request to the given endpoint."
  [dcclient method path data]
  (with-open [ client (http/create-client) ]
    (let [ res (method client (str api-url path) (headers dcclient) data)
           body (-> res http/await http/string) ]
      (if body (json/read-str body) nil))))

;; HTTP Method Definitions

(defn- GET [c u h d] (http/GET c u :headers h :query d))
(defn- PUT [c u h d] (http/PUT c u :headers h :body (json/write-str d)))
(defn- POST [c u h d] (http/POST c u :headers h :body (json/write-str d)))
(defn- PATCH [c u h d] (http/PATCH c u :headers h :body (json/write-str d)))
(defn- DELETE [c u h d] (http/DELETE c u :headers h :query d))

;; Endpoint Definitions

(defmacro defendp [name method path & args]
  (let [ [client data] (map gensym ['client 'data])
         mname (-> name .toLowerCase (.replaceAll " " "-") (.replaceAll "'" "") symbol) ]
    `(defn ~mname ~name [~client ~@args ~data]
      (request ~client ~method (format ~path ~@args) ~data))))

(defendp "Get Guild Audit Logs" GET "/guilds/%s/audit-logs" gid)

(defendp "Get Channel" GET "/channels/%s" cid)
(defendp "Modify Channel" PATCH "/channels/%s" cid)
(defendp "Delete Channel" DELETE "/channels/%s" cid)
(defendp "Get Channel Messages" GET "/channels/%s/messages" cid)
(defendp "Get Channel Message" GET "/channels/%s/messages/%s" cid mid)
(defendp "Create Message" POST "/channels/%s/messages" cid)
(defendp "Create Reaction" PUT "/channels/%s/messages/%s/reactions/%s/@me" cid mid emj)
(defendp "Delete Own Reaction" DELETE "/channels/%s/messages/%s/reactions/%s/@me" cid mid emj)
(defendp "Delete User Reaction" DELETE "/channels/%s/messages/%s/reactions/%s/%s" cid mid emj uid)
(defendp "Get Reactions" GET "/channels/%s/messages/%s/reactions/%s" cid mid emj)
(defendp "Delete All Reactions" DELETE "/channels/%s/messages/%s/reactions" cid mid)
(defendp "Edit Message" PATCH "/channels/%s/messages/%s" cid mid)
(defendp "Delete Message" DELETE "/channels/%s/messages/%s" cid mid)
(defendp "Bulk Delete Messages" POST "/channels/%s/messages/bulk-delete" cid)
(defendp "Edit Channel Permissions" PUT "/channels/%s/permissions/%s" cid oid)
(defendp "Get Channel Invites" GET "/channels/%s/invites" cid)
(defendp "Create Channel Invite" POST "/channels/%s/invites" cid)
(defendp "Delete Channel Permission" DELETE "/channels/%s/permissions/%s" cid oid)
(defendp "Trigger Typing Indicator" POST "/channels/%s/typing" cid)
(defendp "Get Pinned Messages" GET "/channels/%s/pins" cid)
(defendp "Add Pinned Channel Message" PUT "/channels/%s/pins/%s" cid mid)
(defendp "Delete Pinned Channel Message" DELETE "/channels/%s/pins/%s" cid mid)
(defendp "Group DM Add Recipient" PUT "/channels/%s/recipients/%s" cid uid)
(defendp "Group DM Remove Recipient" DELETE "/channels/%s/recipients/%s" cid uid)

(defendp "List Guild Emojis" GET "/guilds/%s/emojis" gid)
(defendp "Get Guild Emoji" GET "/guilds/%s/emojis/%s" gid eid)
(defendp "Create Guild Emoji" POST "/guilds/%s/emojis" gid)
(defendp "Modify Guild Emoji" PATCH "/guilds/%s/emojis/%s" gid eid)
(defendp "Delete Guild Emoji" DELETE "/guilds/%s/emojis/%s" gid eid)

(defendp "Create Guild" POST "/guilds")
(defendp "Get Guild" GET "/guilds/%s" gid)
(defendp "Modify Guild" PATCH "/guilds/%s" gid)
(defendp "Delete Guild" DELETE "/guilds/%s" gid)
(defendp "Get Guild Channels" GET "/guilds/%s/channels" gid)
(defendp "Create Guild Channel" POST "/guilds/%s/channels" gid)
(defendp "Modify Guild Channel Positions" PATCH "/guilds/%s/channels" gid)
(defendp "Get Guild Member" GET "/guilds/%s/members/%s" gid uid)
(defendp "List Guild Members" GET "/guilds/%s/members" gid)
(defendp "Add Guild Member" PUT "/guilds/%s/members/%s" gid uid)
(defendp "Modify Guild Member" PATCH "/guilds/%s/members/%s" gid uid)
(defendp "Modify Current User's Nick" PATCH "/guilds/%s/members/@me/nick" gid)
(defendp "Add Guild Member Role" PUT "/guilds/%s/members/%s/roles/%s" gid uid rid)
(defendp "Remove Guild Member Role" DELETE "/guilds/%s/members/%s/roles/%s" gid uid rid)
(defendp "Remove Guild Member" DELETE "/guilds/%s/members/%s" gid uid)
(defendp "Get Guild Bans" GET "/guilds/%s/bans" gid)
(defendp "Create Guild Ban" PUT "/guilds/%s/bans/%s" gid uid)
(defendp "Remove Guild Ban" DELETE "/guilds/%s/bans/%s" gid uid)
(defendp "Get Guild Roles" GET "/guilds/%s/roles" gid)
(defendp "Create Guild Role" POST "/guilds/%s/roles" gid)
(defendp "Modify Guild Role Positions" PATCH "/guilds/%s/roles" gid)
(defendp "Modify Guild Role" PATCH "/guilds/%s/roles/%s" gid rid)
(defendp "Delete Guild Role" DELETE "/guilds/%s/roles/%s" gid rid)
(defendp "Get Guild Prune Count" GET "/guilds/%s/prune" gid)
(defendp "Begin Guild Prune" POST "/guilds/%s/prune" gid)
(defendp "Get Guild Voice Regions" GET "/guilds/%s/regions" gid)
(defendp "Get Guild Invites" GET "/guilds/%s/invites" gid)
(defendp "Get Guild Integrations" GET "/guilds/%s/integrations" gid)
(defendp "Create Guild Integration" POST "/guilds/%s/integrations" gid)
(defendp "Modify Guild Integration" PATCH "/guilds/%s/integrations/%s" gid iid)
(defendp "Delete Guild Integration" DELETE "/guilds/%s/integrations/%s" gid iid)
(defendp "Sync Guild Integration" POST "/guilds/%s/integrations/%s/sync" gid iid)
(defendp "Get Guild Embed" GET "/guilds/%s/embed" gid)
(defendp "Modify Guild Embed" PATCH "/guilds/%s/embed" gid)

(defendp "Get Invite" GET "/invites/%s" icd)
(defendp "Delete Invite" DELETE "/invites/%s" icd)
(defendp "Accept Invite" POST "/invites/%s" icd)

(defendp "Get Current User" GET "/users/@me")
(defendp "Get User" GET "/users/%s" uid)
(defendp "Modify Current User" PATCH "/users/@me")
(defendp "Get Current User Guilds" GET "/users/@me/guilds")
(defendp "Leave Guild" DELETE "/users/@me/guilds/%s" gid)
(defendp "Get User DMs" GET "/users/@me/channels")
(defendp "Create DM" POST "/users/@me/channels")
(defendp "Create Group DM" POST "/users/@me/channels")
(defendp "Get User Connections" GET "/users/@me/connections")

(defendp "List Voice Regions" GET "/voice/regions")

(defendp "Create Webhook" POST "/channels/%s/webhooks" cid)
(defendp "Get Channel Webhooks" GET "/channels/%s/webhooks" cid)
(defendp "Get Guild Webhooks" GET "/guilds/%s/webhooks" gid)
(defendp "Get Webhook" GET "/webhooks/%s" wid)
(defendp "Get Webhook With Token" GET "/webhooks/%s/%s" wid tkn)
(defendp "Modify Webhook" PATCH "/webhooks/%s" wid)
(defendp "Modify Webhook With Token" PATCH "/webhooks/%s/%s" wid tkn)
(defendp "Delete Webhook" DELETE "/webhooks/%s" wid)
(defendp "Delete Webhook With Token" DELETE "/webhooks/%s/%s" wid tkn)
(defendp "Execute Webhook" POST "/webhooks/%s/%s" wid tkn)
(defendp "Execute Slack-Compatible Webhook" POST "/webhooks/%s/%s/slack" wid tkn)
(defendp "Execute GitHub-Compatible Webhook" POST "/webhooks/%s/%s/github" wid tkn)

(defendp "Get Gateway" GET "/gateway")
(defendp "Get Gateway Bot" GET "/gateway/bot")

(defendp "Get Current Application Information" GET "/oauth2/applications/@me")
