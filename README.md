# Disclojure

A Discord API wrapper for Clojure, aimed at providing a simple, idiomatic, easy-to-use interface for creating Discord bots.

- [Documentation](http://docs.tripl3dogdare.com/disclojure/)

## Disclaimer

Disclojure is still heavily in development, and said development is currently aimed not at ease of use but at completeness. It comes as a use-at-your-own-risk option for advanced users. For example, at time of writing there is no built-in caching support or ratelimit protection. Eventually, once the library is satisfactorily feature-complete, my attention will turn towards ease of use.

## Installation

To install, add the following dependency to your project or build file:

```clojure
[com.tripl3dogdare/disclojure "Indev"]
```

**WARNING: Disclojure is currently in heavy development and changes are not being versioned. It should be considered very unstable as a dependency. Once it is complete enough to be used reliably, proper versioning will be used.**

## Examples

For a full example bot kept reliably up to date with the current state of the library, you can see [example/testbot.clj](https://github.com/tripl3dogdare/disclojure/blob/master/example/testbot.clj).

```clojure
(ns discord-bot
  (:require
    [disclojure.core :as dc]))

; A simple bot that just prints the text of any message it receives to the console
(defn -main []
  (-> (slurp "token.txt") ; Retrieve the token from a separate, .gitignore'd file
      dc/create-client ; Create a new client instance using the token

      ; Create a new listener for the :message event.
      ; The event map recieved by the listener has the keys :data, :type, and :client.
      ; The threading macro here uses the (:key map) syntax
      ;   to access the :data attribute (the raw event payload)
      ;   and from there the :content attribute (the text of the message).
      (dc/on :message #(-> % :data :content println))

      dc/run)) ; Connect the client to Discord and away we go!
```

## Todo (in rough order of priority)

- Caching (WIP)
- Rate limit protection
- Support for outgoing gateway messages (Status Update, Request Guild Members)
- Utility methods
- Command framework
- Extensive testing
- Add to Clojars
- Get channel on Discord API server
- Improve documentation
- Voice support?
