(ns app.state
  (:require [reagent.core :refer [atom]]))

(defonce counter (atom {}))

(defonce user (atom {}))
