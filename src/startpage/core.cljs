(ns startpage.core
  (:require [reagent.core :as r]
            [reagent.debug :as d]
            [startpage.home :refer [startpage]]
            [secretary.core :as secretary :refer-macros [defroute]]))

(defonce current-page (r/atom nil))

(secretary/set-config! :prefix "/")

(defn app-view []
  [:div.container [@current-page]])

(defroute "/" []
  (reset! current-page startpage))

                                        ; the server side doesn't have history, so we want to make sure current-page is populated
(reset! current-page startpage)

(defn on-js-reload
  []
  (reset! current-page startpage))
