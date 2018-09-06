(ns app.events
  (:require [re-frame.core :as rf]
            [app.state :as state]
            [app.fb.db :as fb-db]))

(defn increment
  []
  (fb-db/save! ["counter"] (swap! state/counter inc)))

(defn decrement
  []
  (fb-db/save! ["counter"] (swap! state/counter dec)))

(defn reset
  []
  (fb-db/save! ["counter"] (reset! state/counter 0))

(rf/reg-event-db
 :favorite-client
 (fn [db [_ id]]
     (update-in db [:user :favorite-client] (fnil conj #{}) id)))

(rf/reg-event-db
 :unfavorite-client
 (fn [db [_ id]]
     (update-in db [:user :favorite-client] disj id))))
