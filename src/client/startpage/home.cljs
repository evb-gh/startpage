(ns startpage.home
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :refer [put! alts! chan <! >! timeout close!]]
   [goog.object :as gobj]
   [startpage.srcery :refer [colors]]
   [cljs-http.client :as http]
   [garden.units :as u :refer [px pt pc]]
   [reagent.core :as r]
   [cljs-css-modules.macro :refer-macros [defstyle]]
   [reagent.debug :as d]))


(defstyle reddit-style
  [:.root {:margin 0}])

(defn reddit
  []
  [:div
   {:class (:root reddit-style)}
   "reddit feed here"])

(defn get-org!
  [ref]
  (go
    (let [resp (<! (http/get "/org"))]
      (reset! ref (:body resp)))))

(defstyle org-styles
  [:.root {}
   [:ul {:padding-left (px 20)
         :margin 0}]]
  )

(defn org
  []
  (let [org-data (r/atom [])
        org-updater (js/setInterval
                     #(get-org! org-data)
                     300000)]
    (r/create-class
     {:component-will-mount #(get-org! org-data)
      :component-will-unmount #(js/clearInterval org-updater)
      :reagent-render
      (fn []
        [:div
         {:class (:root org-styles)}
         [:ul
          (for [node @org-data]
            ^{:key (:headline node)}
            [:li (:headline node)])]])})))

(defn get-figlet!
  [ref]
  (go
    (let [time-str (-> (js/Date.) .toTimeString (clojure.string/split " ") first)
          resp (<! (http/post "/figlet" {:json-params {:text time-str}}))]
      (reset! ref (:body resp)))))

(defstyle clock-style
  [:.root {:font-size (px 10)}
   [:pre {:margin 0}]]
  [:.clock {:width (px 450)}])

(defn clock
  []
  (let [ascii (r/atom "")
        time-updater (js/setInterval
                      #(get-figlet! ascii)
                      1000)]
    (r/create-class
     {:component-will-mount #(get-figlet! ascii)
      :component-will-unmount #(js/clearInterval time-updater)
      :reagent-render
      (fn []
        [:div
         {:class (:root clock-style)}
         [:div {:class (:row clock-style)}
          [:pre
           {:class (:clock clock-style)}
           @ascii]]])})))

(defstyle startpage-style
  [:.root {:color (-> colors :bright-white :hex)
           :display "flex"
           :justify-content "space-around"
           :height "100vh"
           :margin (px 30)
           }])

(defn startpage
  []
  (r/create-class
   {:reagent-render
    (fn []
      [:div {:class (:root startpage-style)}
       [org]
       [clock]
        [reddit]
       ])}))
