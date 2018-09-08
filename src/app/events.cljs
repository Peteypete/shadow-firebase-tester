(ns app.events
  (:require [re-frame.core :as rf]
            [app.state :as state]
            [app.fb.db :as fb-db]))

;Original - reagent saves to firebase via fb-db/save!

;(defn increment
;  []
;  (fb-db/save! ["counter"] (swap! state/counter inc)))
;
;(defn decrement
;  []
;  (fb-db/save! ["counter"] (swap! state/counter dec)))
;(defn reset
;  []
;  (fb-db/save! ["counter"] (reset! state/counter 0)))


;rewrite as re-frame event
(rf/reg-event-db
 :increment
 (fn [db [_ id]]
     (fb-db/save! ["counter"] (swap! state/counter inc))))

(rf/reg-event-db
 :decrement
 (fn [db [_ id]]
     (fb-db/save! ["counter"] (swap! state/counter dec))))

(rf/reg-event-db
 :reset
 (fn [db [_ id]]
     (fb-db/save! ["counter"] (reset! state/counter 5))))


;re-frame only updates local state
(rf/reg-event-db
 :favorite-client
 (fn [db [_ id]]
     (update-in db [:user :favorite-client] (fnil conj #{}) id)))

(rf/reg-event-db
  :unfavorite-client
  (fn [db [_ id]]
      (update-in db [:user :favorite-client] disj id)))



;version from backgammon

(defn db-save! [ref data]
  (.set ref data))

(rf/reg-fx
 :firebase/set
 (fn [{:keys [game-id data]}]
   (db-save! (fb-db/db-ref [game-id]) data)))

(rf/reg-event-fx
 :decrement2
  (fn [{:keys [db]} _]
   (let [counter #{}]
     {:firebase/set {:game-id "counter"
                     :data (swap! state/counter dec)}})))

(rf/reg-event-fx
 :increment2
 (fn [{:keys [db]} _]
   (let [counter #{}]
     {:firebase/set {:game-id "counter"
                     :data (swap! state/counter inc)}})))


(rf/reg-event-fx
 :reset2
 (fn [{:keys [db]} _]
   (let [counter #{}]
     {:firebase/set {:game-id "counter"
                     :data (reset! state/counter 3)}})))

(rf/reg-event-fx
 :favorite-client2
 (fn [{:keys [db]} [_ id]]
   (let [id ()]
     {:firebase/set {:game-id "user/favorite-client"
                     :data (swap! state/counter inc)}})))
