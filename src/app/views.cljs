(ns app.views
  (:require [re-frame.core :as rf]
            [app.state :as state]
            [app.events :refer [increment decrement reset]]
            [app.fb.auth :as fb-auth]))

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
                          :action  "File"}
              :client-05 {:id :client-05
                          :name "Newaya, LLC"
                          :return-id "2016:S:S454754160:V2"
                          :desc "Real clients"
                          :form "1120S"
                          :lead "Peter Wen"
                          :accounting "qbo"
                          :status "Ready For Review"
                          :action  "Review"}
              :client-06 {:id :client-06
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
 :active-client
 (fn [db]
  (get-in db [:user :active-client] #{})))

(rf/reg-sub
 :active-client?
 (fn []
   (rf/subscribe [:active-client]))
 (fn [active-client [_ id]]
   (contains? active-client id)))

(rf/reg-event-db
 :initialize-clients
 (fn [db]
   (assoc db :clients clients)))

(rf/reg-event-db
 :active-client
 (fn [db [_ id]]
     (update-in db [:user :active-client] (fnil conj #{}) id)))

(rf/reg-event-db
 :unactive-client
 (fn [db [_ id]]
     (update-in db [:user :active-client] disj id)))

(defn client-component [id client]
  (let [active-client? @(rf/subscribe [:active-client? id])]
     [:div
       [:a {:on-click (fn[e]
                        (.preventDefault e)
                        (if active-client?
                          (rf/dispatch [:unactive-client id])
                          (rf/dispatch [:active-client id])))
            :href "#"
            :style {:color (if active-client?
                             :red
                             :grey)
                    :text-decoration :none}}
        "♥"]
       [:a {:on-click (fn [e]
                        (.preventDefault e)
                        (if active-client?
                          (rf/dispatch [:unactive-client id])
                          (rf/dispatch [:active-client id])))
            :href "#"}
           (:name client)]]))


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
     [:h4 ":active-client state"]
     (pr-str @(rf/subscribe [:active-client]))]]])


(defn active-client-component [id client]
 (let [active-client? @(rf/subscribe [:active-client? id])]
   [:div
    [:div (if active-client?
            [:div.ui.card [:div.content [:div.header (:name client)]
                           [:div.meta (:id client)]
                           [:div.description (:desc client)]]
              [:br]]
      ;[:div [sa/Card {:image {:src "https://source.unsplash.com/random/100x100"} :centered true :header (:name client) :description (:desc client) :meta (:form client) }]]
           [:a])]]))

(defn app []
  [:div
   [header]
   [:div [clients-panel]]
   [counter]])

(rf/dispatch [:initialize-clients])
