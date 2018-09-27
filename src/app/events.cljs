(ns app.events
  (:require [re-frame.core :as rf]
            [app.state :as state]
            [app.fb.db :as fb-db]))

;reagent saves to firebase via fb-db/save!
(defn increment
  []
  (fb-db/save! ["counter"] (swap! state/counter inc)))

(defn decrement
  []
  (fb-db/save! ["counter"] (swap! state/counter dec)))

(defn reset
  []
  (fb-db/save! ["counter"] (reset! state/counter 0)))


;re-frame only updates local state
(rf/reg-event-db
 :favorite-client
 (fn [db [_ id]]
     (update-in db [:user :favorite-client] (fnil conj #{}) id)))

(rf/reg-event-db
 :unfavorite-client
 (fn [db [_ id]]
     (update-in db [:user :favorite-client] disj id)))


;sample write to datbase from David Goldfarb re-frame firebase
(rf/reg-event-fx
  :write-status
  (fn [{db :db} [_ status]]
    {:firebase/write {:path [:status]
                      :value status
                      :on-success #(js/console.log "Wrote status")
                      :on-failure [:handle-failure]}}))
