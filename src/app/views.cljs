(ns app.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [app.state :as state]
            [app.events :refer [increment decrement reset]]
            [com.degel.re-frame-firebase :as re-fire]
            [app.fb.auth :as fb-auth]
            [clojure.string :as str]))


(defn rff-test []
  [:div.ui.padded
      [:div.banner [:h1 "RFF Test"
                    [:div.four.wide.column [:div.ui.huge.primary]
                                           (str @(rf/subscribe [:firebase/on-value {:path [:rff]}]))]]]])


;; TODO move this to a better place

(rf/reg-event-db
 :write-local-debit
 (fn [db [_ newval]]
   (js/console.log ":write-local-debit: " newval)
   (assoc db :debit newval)))

(rf/reg-event-fx
  :read-debit
  (fn [{db :db} [_ status]]
    {:firebase/read-once {:path [:write-test :debit]
                          :on-success [:write-local-debit]
                          :on-failure [:my-empty "read-debit on-failure"]}}))

(rf/reg-sub
 :debit
 (fn [db]
   (get-in db [:debit])))

;; (rf/reg-sub
;;  :double-local-debit
;;  (fn [db]
;;    (get-in db [:double-local-debit])))

;; Cribbed from re-frame todoMVC example

(defn text-input [{:keys [title on-save on-stop]}]
  ;; the rf/subscribe atom is read-only, so we copy it to another atom
  ;; CHECKME is this a race condition? putting the rf/dispatch at the end is flaky
  (let [
        val (r/atom title)
        stop #(do (when on-stop (on-stop %)))
        save #(let [v (-> @val str str/trim)]
                (on-save v)
                (stop %))
        _ (js/console.log "val: " @val)]

    (fn [props]
      [:input (merge (dissoc props :on-save :on-stop :title)
                     {:type        "text"
                      :value       @val
                      :on-blur     save
                      :auto-focus  true
                      :on-change   #(do (reset! val (-> % .-target .-value)))
                      :on-key-down #(case (.-which %)
                                      13 (save %)
                                      27 (stop %)
                                      nil)})])))

(defn header
  []
  [:div.banner
   [:h1 "shadow-cljs + firebase"]
   (if (empty? @state/user)
     [:p "First you need to log-in ..."]
     [:p "... so taht you can change the state."])])

(defn counter
  []
  [:div.jumbotron
   (if (empty? @state/user)
     [:div.banner
      [:p
       [:button.btn {:on-click #(fb-auth/sign-in-with-google)} "Login"]]]
     [:div.banner
      [:p
       [:button.btn {:on-click #(fb-auth/sign-out)} "Logout"]]
      [:button.btn {:on-click #(decrement)} "\u2212"]
      [:button.btn {:on-click #(reset)} @state/counter]
      [:button.btn {:on-click #(increment)} "\u002B"]
      (when-not (= @state/counter 0)
        [:p [:button.btn {:on-click #(reset)} "reset"]])])])

;code based on Eric Normand "Understanding R-frame Lesson 27 and 28"

(def clients {:client-01 {:id :client-01
                          :name "TaxHubTest"
                          :return-id "2016:S:S453454160:V2"
                          :desc "Tax Hub Test"
                          :form "1120"
                          :lead false
                          :accounting "xero"
                          :status "Ready to File"
                          :action  "File"}
              :client-02 {:id :client-02
                          :name "Brandly"
                          :return-id "2016:S:S32854160:V2"
                          :desc "A Corporation"
                          :form "1120"
                          :lead false
                          :accounting "xero"
                          :status "Ready to Map"
                          :action  "Map"}
              :client-03 {:id :client-03
                          :name "Newaya, LLC"
                          :return-id "2016:S:S454754160:V2"
                          :desc "Real clients"
                          :form "1120S"
                          :lead "Peter Wen"
                          :accounting "qbo"
                          :status "Ready For Review"
                          :action  "Review"}
              :client-04 {:id :client-04
                          :name "PartnershipTest"
                          :return-id "2016:S:S434564160:V2"
                          :desc "101 Main St"
                          :form "1065"
                          :lead "Ben Wen"
                          :accounting "xero"
                          :status "Ready to File"
                          :action  "File"}})
(rf/reg-sub
 :clients
 (fn [db]
   (:clients db)))

(rf/reg-sub
 :favorite-client
 (fn [db]
  (get-in db [:user :favorite-client] #{})))

(rf/reg-sub
 :favorite-client?
 (fn []
   (rf/subscribe [:favorite-client]))
 (fn [favorite-client [_ id]]
   (contains? favorite-client id)))

(rf/reg-event-db
 :initialize-clients
 (fn [db]
   (assoc db :clients clients)))

(rf/reg-event-fx
  :write-test
  (fn [{db :db} [_ value & k]]
    {:firebase/write {:path (into [:write-test] k)
                      :value value
                      :on-success #(js/console.log "Wrote " k " value: " value)
                      :on-failure [:my-empty]}}))

(defn editable-td-ytd-hacky-adj
  "key-base is the account name
  c-or-d is a keyword. Either :credit or :debit"
  [key-base c-or-d]
  (let [editing (r/atom false)]
    (fn [key-base c-or-d]
      (let [k (keyword key-base)
            remote-value @(rf/subscribe [:firebase/on-value {:path [:write-test c-or-d k]}])
            remote-label (str (if (empty? remote-value) "0.00" remote-value))]
        [:td {:key (keyword (str key-base "-ytd-hacky-" (name c-or-d) "-adj"))
              :class (str (when @editing "editing"))}
         [:div.view
          [:label {:on-click #(do (reset! editing true))}
           remote-label]]
         (when @editing
           (js/console.log "editing")
           [text-input
            {:class "edit"
             :title remote-label
             :on-save #(if (seq %)
                         (rf/dispatch [:write-test % c-or-d k]))
             :on-stop #(reset! editing false)}])]))))

(defn client-component [id client]
  (let [favorite-client? @(rf/subscribe [:favorite-client? id])]
     [:div
       [:button.btn {:on-click #(rf/dispatch [:unfavorite-client id])} "\u2212"]
       [:button.btn {:on-click #(rf/dispatch [:favorite-client id])} "\u002B"]
       [:a {:on-click (fn[e]
                        (.preventDefault e)
                        (if favorite-client?
                          (rf/dispatch [:unfavorite-client id])
                          (rf/dispatch [:favorite-client id])))
            :href "#"
            :style {:color (if favorite-client?
                             :orange
                             :grey)
                    :text-decoration :none}}
        "★"]

      " "(:name client)]))

(defn clients-panel
  []
  [:div
   [:h2 "Clients Yo"]
   [:div
    (doall
      (for [[id client] @(rf/subscribe [:clients])]
        [:span {:key id}
          [client-component id client]]))
    [:div
     [:h4 ":clients state"]
     (pr-str @(rf/subscribe [:clients]))]
    [:div
     [:h4 ":favorite-client state"]
     (pr-str @(rf/subscribe [:favorite-client]))]]])

(defn app []
  [:div
   [header]
   [clients-panel]
   [counter]
   [rff-test]])

(rf/dispatch [:initialize-clients])
