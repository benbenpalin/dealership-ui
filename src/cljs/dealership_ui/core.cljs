(ns dealership-ui.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [dealership-ui.ajax :as ajax]
    [dealership-ui.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string])
  (:import goog.History))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:common/page-id])) :is-active)}
   title])

(defn navbar [] 
  (r/with-let [expanded? (r/atom false)]
              [:nav.navbar.is-info>div.container
               [:div.navbar-brand
                [:a.navbar-item {:href "/" :style {:font-weight :bold}} "dealership-ui"]
                [:span.navbar-burger.burger
                 {:data-target :nav-menu
                  :on-click #(swap! expanded? not)
                  :class (when @expanded? :is-active)}
                 [:span][:span][:span]]]
               [:div#nav-menu.navbar-menu
                {:class (when @expanded? :is-active)}
                [:div.navbar-start
                 [nav-link "#/" "Home" :home]
                 [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn text-input [label id]
  [:div
   [:label {:for id} label]
   [:input {:type "text" :id id :name id}]])

(defn home-page []
  [:section.section>div.container>div.content
   [:h1 "Car Sale"]
   ;; TODO if existing customer, add search (pr at best, customer ID entry)
   ;; if new customer, show form and add them to customer database
   ;; break up customer and purchase entry forms?
   ;; Pull car Ids from library in a select (dropdown of make, model, year)
   [:div
    [:label {:for "customerStatus"} "Customer Status"]
    [:select {:name "customerStatus" :id "customerStatus"}
     [:option {:value "existing"} "Existing Customer"]
     [:option {:value "new"} "New Customer"]]]
   [:form
    [text-input "First Name" "fame"]
    [text-input "Midde Initial" "minit"]
    [text-input "Last Name" "lname"]
    [text-input "Phone Number" "phone"]
    [text-input "Street Address" "address"]
    [text-input "City" "city"]
    [text-input "State" "state"]
    [text-input "zipcode" "zip"]
    [text-input "Car ID" "carId"]
    [text-input "Sale Price" "price"]
    [:div
     [:label {:for "date"} "Date of Sale"]
     [:input {:type "date" :id "date" :name "date"}]]
    [:input {:type "submit" :value "Complete Sale"}]]])

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [navbar]
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {}))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
