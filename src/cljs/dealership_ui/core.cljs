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
    [clojure.set :as cset]
    [cljs-time.core :as time]
    [cljs-time.format :as timef])
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

(defn text-input [label id event]
  [:div
   [:label {:for id} label]
   [:input {:type "text" :id id :name id :on-change #(rf/dispatch [event id (-> % .-target .-value)])}]])

(defn new-customer-inputs [event]
  [:div
   [text-input "First Name" :firstName event]
   [text-input "Midde Initial" :middleInitial event]
   [text-input "Last Name" :lastName event]
   [text-input "Phone Number" :phoneNumber event]
   [text-input "Street Address" :streetAddress event]
   [text-input "City" :city event]
   [text-input "State" :state event]
   [text-input "Zip Code" :zipcode event]])

(def styles
  {:button {:border "2px solid black" :max-width "100px"}})

(defn sale-bill []
  (let [{:keys [customerNames purchaseId dateOfSale make model year color licensePlateNumber licensePlateState]} @(rf/subscribe [:sale/bill])
        sale-price @(rf/subscribe [:sale/salePrice])]
    [:div
     [:h4 "Bill"]
     [:div (str dateOfSale)]
     [:div (str "Customers: " (string/join ", " customerNames))]
     [:div (str "PurchaseId: " purchaseId)]
     [:div (str "Car Purchase: " year " " make " " model)]
     [:div (str "Sale Price: $" sale-price)]
     [:div (str "Color: " color)]
     [:div (str "License Plate: " licensePlateState " " licensePlateNumber)]]))

(defn home-page []
  (let [customer-status @(rf/subscribe [:sale/customer-status])
        num-cust @(rf/subscribe [:sale/number-of-customers])]
   [:section.section>div.container>div.content
    [:h1 "Car Sale"]
    ;; TODO add ability to add another customer
    [:div
     [:label {:for "numberOfCustomers"} "How Many Purchasers"]
     [:select {:name "numberOfCustomers"
               :id "numberOfCustomers"
               :on-change #(rf/dispatch [:change-number-of-customers (-> % .-target .-value)])}
      [:option {:value "one"} "One"]
      [:option {:value "two"} "Two"]]]
    [:dev
     [:label {:for "customerStatus"} "Status of Customer(s)"]
     [:select {:name "customerStatus"
               :id "customerStatus"
               :on-change #(rf/dispatch [:change-sale-customer-status (-> % .-target .-value)])}
      [:option {:value "existing"} "Existing"]
      [:option {:value "new"} "New"]]
     [:br]]
    [:div {:style {:margin-top "20px"}} "Input Customer Information"]
    [:form
     (if (= customer-status "new")
       [:div
        [new-customer-inputs :update-sale-customer-1-value]
        (when (= num-cust "two")
          [:div
           [:br]
           [:div "Second Customer Information"]
           [new-customer-inputs :update-sale-customer-2-value]])]
       [:div
        [text-input "Customer ID" :customerId1 :update-sale-customer-val]
        (when (= num-cust "two")
          [text-input "2nd Customer ID" :customerId2 :update-sale-customer-val])])
     [:br]
     [text-input "Car ID" :carId :update-sale-val]
     [text-input "Sale Price" :salePrice :update-sale-val]
     [:br]
     [:div {:style (:button styles) :on-click #(rf/dispatch [:submit-purchase])} "Complete Purchase"]]
    [sale-bill]]))

(defn sales-row [{:keys [vehicleId make model year totalSold profit]}]
  [:tr
   [:td vehicleId]
   [:td make]
   [:td model]
   [:td year]
   [:td totalSold]
   [:td profit]])

(defn report-page []
  (let [report @(rf/subscribe [:sales-report/report])
        start-date @(rf/subscribe [:sales-report/start-date])
        end-date @(rf/subscribe [:sales-report/end-date])]

    [:section.section>div.container>div.content
     [:h1 "Report"]
     [:div
      [:form
       [:div
        [:label {:for "startDate"} "Start Date"]
        [:input {:type "date" :id "startDate" :name "startDate" :value start-date
                 :on-change #(rf/dispatch [:set-start-date (-> % .-target .-value)])}]]
       [:div
        [:label {:for "endDate"} "End Date"]
        [:input {:type "date" :id "endDate" :name "endDate" :value end-date
                 :on-change #(rf/dispatch [:set-end-date (-> % .-target .-value)])}]]
       [:div
        ;;TODO get border working (probably make a class for buttons, since buttons suck)
        [:div {:style (:button styles) :on-click #(rf/dispatch [:pull-report])} "Pull Report"]]]]
     [:div
      [:table
       [:thead
        [:th "Vehicle Id"]
        [:th "Make"]
        [:th "Model"]
        [:th "Year"]
        [:th "Total Sold"]
        [:th "Profit"]]
       [:tbody
        (map sales-row report)]]]]))

(defn make-timeslot-option [{:keys [timeslotId startTime endTime]}]
  [:option {:value timeslotId} (str startTime " to " endTime)])

(defn make-package-option [{:keys [packageId name]}]
  [:option {:value packageId} name])

(defn make-checkbox [{:keys [taskName taskId checked]} taskPackageRelation]
  [:div
   [:input {:type "checkbox" :id taskId :name taskName :value taskId :checked checked
            :on-change #(rf/dispatch [:update-check taskPackageRelation taskId checked])}]
   [:label {:for taskId} taskName]])

(defn make-vehicle-type-option [{:keys [make model year vehicleId]}]
  [:option {:value vehicleId} (str make " " model " " year)])

(defn book-page []
  (let [customer-status @(rf/subscribe [:book/customer-status])
        car-status @(rf/subscribe [:book/car-status])
        numCust @(rf/subscribe [:book/:number-of-customers])
        packages @(rf/subscribe [:packages])
        vehicleTypes @(rf/subscribe [:vehicleTypes])
        package-tasks @(rf/subscribe [:packageTasks])
        timeslots @(rf/subscribe [:book/timeslots])
        appointmentId @(rf/subscribe [:book/appointmentId])]
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
      (when (= customer-status "new")
        [:div
         [:div
          [:label {:for "numberOfCustomers"} "How Many New Customers"]
          [:select {:name "numberOfCustomers"
                    :id "numberOfCustomers"
                    :on-change #(rf/dispatch [:change-book-number-of-customers (-> % .-target .-value)])}
           [:option {:value "one"} "One"]
           [:option {:value "two"} "Two"]]]
         [:div "Enter new customer data"]
         [:form
          [new-customer-inputs :update-book-customer-1-value]]
         (when (= numCust "two")
           [:div
            [:br]
            [:div "Second Customer Information"]
            [new-customer-inputs :update-book-customer-2-value]])])
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
         [:div "Enter new car data"]
         [:form
          [:label {:for "make"} "Make"]
          [:select {:name "make" :id "make"
                    :on-change #(rf/dispatch [:update-book-vehicle-id (-> % .-target .-value)])}
           [:option {:value "no car"}]
           (map make-vehicle-type-option vehicleTypes)]
          [text-input "License Plate State" :licensePlateState :update-book-car-value]
          [text-input "License Plate Number" :licensePlateNumber :update-book-car-value]
          [text-input "Color" :color :update-book-car-value]
          [text-input "Odometer" :odometer :update-book-car-value]]]
        [text-input "Car ID" :carId :update-book-car-value])
      [:br]
      ;; All Cases: selects package
      [:form
       [:label {:for "package"} "Choose A Package"]
       [:select {:name "package"
                 :id "package"
                 :on-change #(rf/dispatch [:change-package (-> % .-target .-value)])}
        [:option {:value "no package"}]
        (map make-package-option packages)]]
      ;; Once a package is selected, user will see services included in that package and can choose to uncheck them
      (if (:loaded package-tasks)
        [:div
         [:div "Remove any undesired tasks"]
         [:form
           (map #(make-checkbox  % :inPackage) (:inPackage package-tasks))]
         ;; In all cases, user can add more tasks
         [:br]
         [:div "Add any additional services"]
         [:form
          (map #(make-checkbox % :notInPackage) (:notInPackage package-tasks))]
         ;; calculate total time for appointment
         [:br]
         ;; only show dates with available timeslots of that length (rounded up)
         [:form
          [:label {:for "appt-date"} "Enter A Date Date"]
          [:input {:type "date" :id "appt-date" :name "appt-date"
                   :on-change #(rf/dispatch [:update-date-of-service (-> % .-target .-value)])}]
          [:div {:style (:button styles) :on-click #(rf/dispatch [:get-timeslots])} "Get Time Slots"]]
         ;; only show timeslots on the date selected of the estimated length
         [:form
          [:label {:for "timeslot"} "Time Slot"]
          [:select {:name "timeslot" :id "timeslot"
                    :on-change #(rf/dispatch [:change-selected-timeslot (-> % .-target .-value)])}
           [:option {:value "no-timeslot"}]
           (map make-timeslot-option timeslots)]]
         [:br]
         [:div {:style (:button styles) :on-click #(rf/dispatch [:book-appointment])} "Book Appointment"]
         (if appointmentId
           [:div (str "AppointmentId: " appointmentId)])])]]))

(def part "Spark Plug")


(defn select-appointment [event]
  (let  [appointments @(rf/subscribe [:appointments])]
    [:div
     (for [a appointments]
       (let [{appointmentId :appointmentId color :color year :year make :make model :model vehicleId :vehicleId} a]
         [:div
          [:span {:style {:color "blue" :cursor "pointer"} :on-click  #(rf/dispatch [event appointmentId])} appointmentId]
          [:span (str " " color " " year " " make " " model " ")]]))]))

(defn update-page []
  (let [update-tasks @(rf/subscribe [:update/updateTasks])
        test-tasks (:tests update-tasks)
        replacement-tasks (:partReplacements update-tasks)
        task-success @(rf/subscribe [:update/task-success])
        selected-task @(rf/subscribe [:update/selected-task])
        successful @(rf/subscribe [:update/update-successful])]
    [:section.section>div.container>div.content
     [:h1 "Update Service Record"]
     [:div
      [select-appointment :get-appointment-tasks]
      [:br]
      (when task-success
        [:div
         [:div "Select a Task to Update"]
         [:div
          [:h3 "Part Replacements"]
          [:div (for [{:keys [taskId taskName partId partName costOfPart ]} replacement-tasks]
                  [:div
                   [:div {:on-click #(rf/dispatch [:update-update-selected-task taskId])} taskName]
                   (when (= taskId selected-task)
                     [:div
                      [:div
                       {:on-click #(rf/dispatch [:add-part-to-bill partId taskId])}
                       (str  "Add " partName " - $" costOfPart " -" " to Bill, and Mark Replacement Complete?")]
                      (when successful
                        [:div (str partName " has been added to the bill")])
                      [:br]])])]]
         [:br]
         [:div
          [:h3 "Tests"]
          [:div (for [{:keys [taskId taskName testFailureTaskId testFailureTaskName]} test-tasks]
                  [:div
                   [:div {:on-click #(rf/dispatch [:update-update-selected-task taskId])} taskName]
                   (when (= taskId selected-task)
                     [:div
                      [:div "Did the test Pass?"
                       [:span {:on-click #(rf/dispatch [:complete-task taskId true "Passed"])} "Yes"]
                       [:span {:on-click #(rf/dispatch [:add-task-for-test-failure testFailureTaskId])} "No"]]
                      (when successful
                        [:div (str taskName " has been marked as complete")])])
                   [:br]])]]])]]))

(defn bill-page []
  [:section.section>div.container>div.content
   [:h1 "Bill"]
   [:div
    [select-appointment :tbd]
    [:a "End Appointment and Create Bill"]
    [:div "BILL"]]])

;;Not sure exactly how to handle arrival, not sure what is expected
(defn arrival-page []
  (let [{:keys [appointmentId success]} @(rf/subscribe [:dropoff])]
    [:section.section>div.container>div.content
     [:h1 "Arrival"]
     [:div
      [:div "Which appointment has arrived?"]
      [select-appointment :dropoff-car]
      (when success
        [:div (str "Appointment " appointmentId " has been marked as \"Dropped Off\"")])]]))

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
  (rf/dispatch-sync [:get-packages-on-load])
  (rf/dispatch-sync [:get-appointments-on-load])
  (rf/dispatch-sync [:get-vehicle-types-on-load])
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
