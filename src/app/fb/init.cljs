(ns app.fb.init
  (:require ["firebase/app" :as firebase]
            ["firebase/database"]
            ["firebase/auth"]
            [app.fb.auth :as fb-auth]))

;; == firebase-init ===========================================================
;; Initialize default app. Retrieve your own options values by adding a web app
;; on https://console.firebase.google.com
;;
;; usage: (firebase-init))
;;
(defn firebase-init
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
