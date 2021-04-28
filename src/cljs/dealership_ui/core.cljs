(ns dealership-ui.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [markdown.core :refer [md->html]]
    [dealership-ui.ajax :as ajax]
    [dealership-ui.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [clojure.string :as string]
    [clojure.set :as cset])
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
                 [nav-link "#/report" "Report" :report]
                 [nav-link "#/book" "Book" :book]
                 [nav-link "#/update" "Update" :update]
                 [nav-link "#/bill" "Bill" :bill]
                 [nav-link "#/arrival" "Arrival" :arrival]]]]))

(defn text-input [label id]
  [:div
   [:label {:for id} label]
   [:input {:type "text" :id id :name id}]])

(defn new-customer-inputs []
  [:div
   [text-input "First Name" "fame"]
   [text-input "Midde Initial" "minit"]
   [text-input "Last Name" "lname"]
   [text-input "Phone Number" "phone"]
   [text-input "Street Address" "address"]
   [text-input "City" "city"]
   [text-input "State" "state"]
   [text-input "Zip Code" "zip"]])

(defn home-page []
  (let [customer-status @(rf/subscribe [:sale/customer-status])]
   [:section.section>div.container>div.content
    [:h1 "Car Sale"]
    ;; TODO add ability to add another customer
    [:div
     [:label {:for "customerStatus"} "Customer Status"]
     [:select {:name "customerStatus"
               :id "customerStatus"
               :on-change #(rf/dispatch [:change-sale-customer-status (-> % .-target .-value)])}
      [:option {:value "existing"} "Existing Customer"]
      [:option {:value "new"} "New Customer"]]]
    [:form
     (if (= customer-status "new")
       [new-customer-inputs]
       [text-input "Customer ID" "customerID"])
     [text-input "Car ID" "carId"]
     [text-input "Sale Price" "price"]
     [:div
      [:label {:for "date"} "Date of Sale"]
      [:input {:type "date" :id "date" :name "date"}]]
     [:input {:type "submit" :value "Complete Sale"}]]]))

(def sales-data
 [{:vehicle-id "1234" :make "Subaru" :model "Forester" :year 2015 :total-sold 100 :profit 100000}
  {:vehicle-id "2345" :make "VW" :model "Beatle" :year 1965 :total-sold 20 :profit 200000}])

(defn sales-row [{:keys [vehicle-id make model year total-sold profit]}]
  [:tr
   [:td vehicle-id]
   [:td make]
   [:td model]
   [:td year]
   [:td total-sold]
   [:td profit]])

(defn report-page []
  [:section.section>div.container>div.content
   [:h1 "Report"]
   [:div
    [:form
     [:div
      [:label {:for "startDate"} "Start Date"]
      [:input {:type "date" :id "startDate" :name "startDate"}]]
     [:div
      [:label {:for "endDate"} "End Date"]
      [:input {:type "date" :id "endDate" :name "endDate"}]]]]
   [:div
    [:table
     [:tr
      [:th "Vehicle Id"]
      [:th "Make"]
      [:th "Model"]
      [:th "Year"]
      [:th "Total Sold"]
      [:th "Profit"]]
     (map sales-row sales-data)]]])

(def make-list ["Subaru" "BMX" "VW"])

(def model-list ["Forester" "X3" "Van"])

(def year-list [2015 2016 2017])

(def packages ["No Package" "1 year" "2 year" "3 year"])

(def package-tasks ["brake test" "fluid test" "brake replacement" "spark plug replacement" "oil change"])

(def all-tasks ["brake test" "fluid test" "brake replacement" "spark plug replacement" "oil change" "smoke test" "tire test" "tire replacement" "engine replacement" "wiper change"])

(def time-slots ["1 to 3" "3 to 5" "7 to 10"])

(defn make-option [option]
  [:option {:value option} option])

(defn make-checkbox [item]
  [:div
   [:input {:type "checkbox" :id item :name item :value item}]
   [:label {:for item} item]])


(defn book-page []
  (let [customer-status @(rf/subscribe [:book/customer-status])
        car-status @(rf/subscribe [:book/car-status])
        package @(rf/subscribe [:book/package])]
    [:section.section>div.container>div.content
     [:h1 "Book Service Appointment"]
     [:div
      [:label {:for "customerStatus"} "Customer Status"]
      [:select {:name "customerStatus"
                :id "customerStatus"
                :on-change #(rf/dispatch [:change-book-customer-status (-> % .-target .-value)])}
       [:option {:value "existing"} "Existing Customer"]
       [:option {:value "new"} "New Customer"]]
      ;New Customer
      ;For new customer only customer data
      ;; TODO add ability to add another customer
      (if (= customer-status "new")
        [:div
         [:div "Enter new customer data"]
         [:form
          [new-customer-inputs]]]
        [text-input "Customer ID" "customerId"])
      [:br]
      [:form
       [:label {:for "carStatus"} "Car Status"]
       [:select {:name "carStatus"
                 :id "carStatus"
                 :on-change #(rf/dispatch [:change-book-car-status (-> % .-target .-value)])}
        [:option {:value "existing"} "Car In System"]
        [:option {:value "new"} "Car Not In System"]]]
      ;; For new customer or existing customer with new car
      (if (= car-status "new")
        [:div
         ;; TODO make this just one drop down with vehicle types
         [:div "Enter new car data"]
         [:form
          [:label {:for "make"} "Make"]
          [:select {:name "make" :id "make"}
           (map make-option make-list)]
          [:label {:for "model"} "Model"]
          [:select {:name "model" :id "model"}
           (map make-option model-list)]
          [:label {:for "year"} "Year"]
          [:select {:name "year" :id "year"}
           (map make-option year-list)]
          [text-input "License Plate State" "plate-state"]
          [text-input "License Plate Number" "plate-number"]
          [text-input "Color" "color"]
          [text-input "Odometer" "odometer"]]]
        [text-input "Car ID" "carId"])
      [:br]
      ;; All Cases: selects package
      [:form
       [:label {:for "package"} "Choose A Package"]
       [:select {:name "package"
                 :id "package"
                 :on-change #(rf/dispatch [:change-package (-> % .-target .-value)])}
        (map make-option packages)]]
      ;; Once a package is selected, user will see services included in that package and can choose to uncheck them
      (if (not= package "No Package")
        [:div
         [:div "Remove any unwanted tasks"]
         [:form
           (map make-checkbox package-tasks)]
         ;; In all cases, user can add more tasks
         [:br]
         [:div "Add any additional services"]
         [:form
          (map make-checkbox (vec (cset/difference (set all-tasks) (set package-tasks))))]
         ;; calculate total time for appointment
         [:br]
         ;; only show dates with available timeslots of that length (rounded up)
         [:div "Choose a time slot"]
         [:form
          [:label {:for "appt-date"} "Appointment Date"]
          [:input {:type "date" :id "appt-date" :name "appt-date"}]]
         ;; only show timeslots on the date selected of the estimated length
         [:form
          [:label {:for "timeslot"} "Time Slot"]
          [:select {:name "timeslot" :id "timeslot"}
           (map make-option time-slots)]]])]]))

(def appointments [{:id "123" :car "Gray 2015 Subaru Forester"} {:id "234" :car "Black 2020 VW Bus"}])

(def part "Spark Plug")

(defn select-appointment []
  [:div
   (for [a appointments]
     [:div
      [:a {:href "https://www.google.com"} (:id a)]
      [:span (:car a)]])])

(defn update-page []
  [:section.section>div.container>div.content
   [:h1 "Update Service Record"]
   [:div
    [select-appointment]
    [:div
     [:div "Select a Task to Update"]
     (for [t package-tasks]
       [:div
        [:a {:href "https://www.google.com"} t]])
     [:div "(for part replacement)"]
     [:a {:href "https://www.google.com"} "Add " part " $20.00" " to Bill and Mark Replacement Complete?"]]
    [:br]
    [:div
     [:div "(for test)"]
     [:div "Did the test pass?"]
     [:a {:href "https://www.google.com"} "Yes"]
     [:a {:href "https://www.google.com"} "No"]
     [:div (str "Part to replace, due to failure: " part)]
     [:a {:href "https://www.google.com"} (str "Add " "Spark Plug Replacement " "to tasks?")]]]])

(defn bill-page []
  [:section.section>div.container>div.content
   [:h1 "Bill"]
   [:div
    [select-appointment]
    [:a "End Appointment and Create Bill"]
    [:div "BILL"]]])
    ;In bill
    ; Customer
    ; Date
    ; Car
    ; tests and test status, plus labor cost
    ; Part replacement, labor cost, part name, part cost
    ; Time in
    ; Time out

;;Not sure exactly how to handle arrival, not sure what is expected
(defn arrival-page []
  [:section.section>div.container>div.content
   [:h1 "Arrival"]
   [:div
    [:div "Which appointment has arrived?"]
    [select-appointment]]])

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
           :view        #'home-page}]
     ["/report" {:name :report
                 :view #'report-page}]
     ["/book" {:name :book
               :view #'book-page}]
     ["/update" {:name :update
                 :view #'update-page}]
     ["/bill" {:name :bill
               :view #'bill-page}]
     ["/arrival" {:name :arrival
                  :view #'arrival-page}]]))

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
  (rf/dispatch-sync [:initialize-db])
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
