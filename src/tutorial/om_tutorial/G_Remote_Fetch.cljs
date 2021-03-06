(ns om-tutorial.G-Remote-Fetch
  (:require-macros
    [cljs.test :refer [is]]
    )
  (:require [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [devcards.core :as dc :refer-macros [defcard defcard-doc]]
            ))

(def sample-query [{:widget [(with-meta {:people [:person/name]} {:query-root true})]}])
(def sample-server-response {:people [{:person/name "Joe"}]})
(def server-query (:query (om/process-roots sample-query)))
(def rewrite-function (:rewrite (om/process-roots sample-query)))
(def restructured-response (rewrite-function sample-server-response))

(defcard-doc
  "
  # Remote Fetch

  TODO

  ### Remote Fetch

  For each remote that you list in the reconciler (default is just `:remote`), the parser will run with `:target` set
  in the env to that remote. This lets you gather up different sets of queries to send to each remote.

  The reader factory I've created lets you supply a map from remote name to reader function, so that you can
  separate your logic out for each of these query parses.

  In remote parsing mode, the parser expects you to return either `true` or a (possibly modified) AST node (which
  comes in as `:ast` in `env`). Doing recursive parsing on this is a bit of a pain, but is also typically necessary
  so that you can both maintain the structure of the query (which *must* be rooted from your Root component)
  and prune out the bits you don't want.

  The remote read in this example (so far) only wants a list of people. Everything else is client-local. Using the
  parsing helpers in the `om-tutorial.parsing` namespace, this pares down to this:

  ```
  (defn read-remote [env key params]
    (case key
      :widget (p/recurse-remote env key true)
      :people (p/fetch-if-missing env key :make-root)
      :not-remote ; prune everything else from the parse
      )
    )
  ```

  The `recurse-remote` function basically means \"I have to include this node, because it is on the path to real
    remote data, but it itself needs nothing from the server\". The `fetch-if-missing` function has quite a bit
  of logic in it, but basically means \"Everything from here down is valid to ask the server about\".

  The `:make-root` flag (which can be boolean or any other keyword, but only has an effect if it is `:make-root` or `true`)
  is used to set up root processing. I'll cover that more later.

  TODO: Elide keywords from the resulting fetch query if they are in the ui.* namespace, so we don't ask the server for them

  ## Re-rooting Server Queries

  In our tutorial application we have a top-level component that queries for `:widget`. The queries must compose to
  the root of the UI, but we'd really like to not have to send this client-local bit of the query over to the server,
  as it would mean we'd have to have the server understand what every UI is doing structurally.

  To address this your read function can return an AST node which has been expanded to include `:query-root true`. This will cause
  the parser to mark that node as the intended root of the server query.

  Now, when your send function is called, it will be called with two parameters: The remote-keyed query map:

  "
  {:my-server sample-query}
  "
  and a callback function as a second argument that expects to be given the response which must have the state-tree structure:
  "
  {:widget sample-server-response}
  "
  but what you'd like to do is send this to the server:
  "
  server-query
  "
  get this back:
  "
  sample-server-response
  "
  and then transform it to this:
  "
  restructured-response
  "
  before calling the callback to give the data to Om. (Note: The data above is being generated by code in this secton's
  file. You can play with it by editing the source.)

  If you've returned `:query-root true` during the parse phase at the `:people` node, then Om will have marked that
  portion of the query with metadata such that you can use `om/process-roots` to strip off (and then re-add) the
  UI-specific query prefix.

  ```
  (defn send [queries callback]
    (let [full-query (:remote queries)
          {:keys [re-rooted-query rewrite]} (om/process-roots full-query)
          server-response (send-to-server re-rooted-query)
          restructured-response (rewrite server-response)]
          (callback restructured-response)))
  ```
  ")

(defcard-doc "

  ### Server simulation

  The present example has a server simulation (using a 1 second setTimeout). Hitting \"refresh\" will clear the `:people`,
  which will cause the remote logic to trigger. One second later you should see the simulated data I've placed on this
  \"in-browser server\".

  There is a lot more to do here, but tempids are not quite done yet, so I'll add more in as that becomes available.
  ")
