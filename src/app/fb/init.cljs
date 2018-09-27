(ns app.fb.init
  (:require ["firebase/app" :as firebase]
            ["firebase/database"]
            ["firebase/auth"]
            [com.degel.re-frame-firebase :as re-fire]
            [app.fb.auth :as fb-auth]))

;; TODO nominally security, move these keys and urls separate from source

(defonce firebase-app-info
  {:apiKey "AIzaSyDQsn5IIaUlpik1aUlDNaBwXXRPVBElxlQ",
   :authDomain "fir-tester-39c24.firebaseapp.com",
   :databaseURL  "https://fir-tester-39c24.firebaseio.com",
   :projectId  "fir-tester-39c24",
   :storageBucket  "fir-tester-39c24.appspot.com"})

;; == firebase-init ===========================================================
;; Initialize default app. Retrieve your own options values by adding a web app
;; on https://console.firebase.google.com
;;
;; usage: (firebase-init))
;;
(defn firebase-init
  "Initialize re-frame-firebase (implicitly firebase.js)"
  []
  (if (zero? (alength firebase/apps))
    (re-fire/init :firebase-app-info      firebase-app-info
                  :firestore-settings     {:timestampsInSnapshots true}
                  :get-user-sub           []
                  :set-user-event         []
                  :default-error-handler  [])
    (firebase/app))
  (fb-auth/on-auth-state-changed))

(defn firebase-init-deprecated
  []
  (if (zero? (alength firebase/apps))
    (firebase/initializeApp
     #js { :apiKey "AIzaSyDQsn5IIaUlpik1aUlDNaBwXXRPVBElxlQ",
           :authDomain "fir-tester-39c24.firebaseapp.com",
           :databaseURL  "https://fir-tester-39c24.firebaseio.com",
           :projectId  "fir-tester-39c24",
           :storageBucket  "fir-tester-39c24.appspot.com",
           :messagingSenderId  "320240248272"})
    (firebase/app))
  (fb-auth/on-auth-state-changed))
