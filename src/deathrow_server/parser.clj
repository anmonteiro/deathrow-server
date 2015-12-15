(ns deathrow-server.parser
  (:require [om.next.server :as om]))

;; =============================================================================
;; Reads

(defmulti readf om/dispatch)

(defmethod readf :default
  [_ k _]
  {:value {:error (str "No handler for key " k)}})

(defn create-parser []
  (om/parser {:read readf}))
