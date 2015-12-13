(ns deathrow-server.parser
  (:require [om.next.server :as om]))

;; =============================================================================
;; Reads

(defn dispatch
  "Helper function for implementing :read and :mutate as multimethods. Use this
   as the dispatch-fn."
  [_ key _] key)

(defmulti readf dispatch)

(defmethod readf :default
  [_ k _]
  {:value {:error (str "No handler for key " k)}})

(defn create-parser []
  (om/parser {:read readf}))
