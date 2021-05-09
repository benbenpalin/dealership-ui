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
                 [nav-link "#/" "Sale" :home]
                 [nav-link "#/report" "Report" :report]
                 [nav-link "#/book" "Book" :book]
                 [nav-link "#/update" "Update" :update]
                 [nav-link "#/bill" "Bill" :bill]
                 [nav-link "#/arrival" "Arrival" :arrival]]]]))

(def styles
  {:button {:border-radius "5px" :max-width "200px" :text-align "center" :cursor "pointer"
            :background "blue" :color "white"}
   :home-label {:display "inline-block" :width "200px"}})

(defn text-input [label id event]
  [:div
   [:label {:for id :style (:home-label styles)} label]
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

(defn sale-bill []
  (let [{:keys [customerNames purchaseId dateOfSale make model year color licensePlateNumber licensePlateState]} @(rf/subscribe [:sale/bill])
        sale-price @(rf/subscribe [:sale/salePrice])]
    [:div {:style {:margin-top "30px"}}
     [:h5 "Bill"]
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
    [:div
     [:label {:for "numberOfCustomers" :style (:home-label styles)} "How Many Purchasers"]
     [:select {:name "numberOfCustomers"
               :id "numberOfCustomers"
               :on-change #(rf/dispatch [:change-number-of-customers (-> % .-target .-value)])}
      [:option {:value "one"} "One"]
      [:option {:value "two"} "Two"]]]
    [:dev
     [:label {:for "customerStatus" :style (:home-label styles)} "Status of Customer(s)"]
     [:select {:name "customerStatus"
               :id "customerStatus"
               :on-change #(rf/dispatch [:change-sale-customer-status (-> % .-target .-value)])}
      [:option {:value "existing"} "Existing"]
      [:option {:value "new"} "New"]]
     [:br]]
    [:h5 {:style {:margin-top "20px"}} "Enter Customer Information"]
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
     [:h5 {:style {:margin-top "20px"}} "Enter Sale Information"]
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
            :style {:margin-right "20px"}
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
      [:h5 {:style {:margin-top "20px"}} "Customer Information"]
      [:label {:for "customerStatus" :style (:home-label styles)} "Customer Status"]
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
          [:label {:for "numberOfCustomers" :style (:home-label styles)} "How Many New Customers"]
          [:select {:name "numberOfCustomers"
                    :id "numberOfCustomers"
                    :on-change #(rf/dispatch [:change-book-number-of-customers (-> % .-target .-value)])}
           [:option {:value "one"} "One"]
           [:option {:value "two"} "Two"]]]
         [:h5  {:style {:margin-top "20px"}} "Enter New Customer Information"]
         [:form
          [new-customer-inputs :update-book-customer-1-value]]
         (when (= numCust "two")
           [:div
            [:h5  {:style {:margin-top "20px"}} "Enter Second Customer Information"]
            [new-customer-inputs :update-book-customer-2-value]])])
      [:form
       [:h5  {:style {:margin-top "20px"}} "Enter Car Information"]
       [:label {:for "carStatus" :style (:home-label styles)} "Car Status"]
       [:select {:name "carStatus"
                 :id "carStatus"
                 :on-change #(rf/dispatch [:change-book-car-status (-> % .-target .-value)])}
        [:option {:value "existing"} "Car In System"]
        [:option {:value "new"} "Car Not In System"]]]
      ;; For new customer or existing customer with new car
      (if (= car-status "new")
        [:div
         [:h5  {:style {:margin-top "20px"}} "Enter New Car Information"]
         [:form
          [:label {:for "vehicle-type" :style (:home-label styles)} "Vehicle Type"]
          [:select {:name "vehicle-type" :id "vehicle-type"
                    :on-change #(rf/dispatch [:update-book-vehicle-id (-> % .-target .-value)])}
           [:option {:value "no car"}]
           (map make-vehicle-type-option vehicleTypes)]
          [text-input "License Plate State" :licensePlateState :update-book-car-value]
          [text-input "License Plate Number" :licensePlateNumber :update-book-car-value]
          [text-input "Color" :color :update-book-car-value]
          [text-input "Odometer" :odometer :update-book-car-value]]]
        [text-input "Car ID" :carId :update-book-car-value])
      ;; All Cases: selects package
      [:form
       [:h5  {:style {:margin-top "20px"}} "Select Package Information"]
       [:label {:for "package" :style (:home-label styles)} "Choose A Package"]
       [:select {:name "package"
                 :id "package"
                 :on-change #(rf/dispatch [:change-package (-> % .-target .-value)])}
        [:option {:value "no package"}]
        (map make-package-option packages)]]
      ;; Once a package is selected, user will see services included in that package and can choose to uncheck them
      (if (:loaded package-tasks)
        [:div
         [:h5  {:style {:margin-top "20px"}} "Remove Any Undesired Tasks"]
         [:form
           (map #(make-checkbox  % :inPackage) (:inPackage package-tasks))]
         ;; In all cases, user can add more tasks
         [:h5  {:style {:margin-top "20px"}} "Add Any Additional Tasks"]
         [:form
          (map #(make-checkbox % :notInPackage) (:notInPackage package-tasks))]
         ;; calculate total time for appointment
         ;; only show dates with available timeslots of that length (rounded up)
         [:form
          [:h5  {:style {:margin-top "20px"}} "Select Time Slot"]
          [:label {:for "appt-date" :style (:home-label styles)} "Enter A Date Date"]
          [:input {:type "date" :id "appt-date" :name "appt-date"
                   :on-change #(rf/dispatch [:update-date-of-service (-> % .-target .-value)])}]
          [:div {:style (merge (:button styles) {:margin-top "20px" :margin-bottom "20px"}) :on-click #(rf/dispatch [:get-timeslots])} "Get Time Slots"]]
         ;; only show timeslots on the date selected of the estimated length
         [:form
          [:label {:for "timeslot" :style (:home-label styles)} "Time Slot"]
          [:select {:name "timeslot" :id "timeslot"
                    :on-change #(rf/dispatch [:change-selected-timeslot (-> % .-target .-value)])}
           [:option {:value "no-timeslot"}]
           (map make-timeslot-option timeslots)]]
         [:div {:style (merge (:button styles) {:margin-top "20px" :margin-bottom "20px"}) :on-click #(rf/dispatch [:book-appointment])} "Book Appointment"]
         (if appointmentId
           [:h4 (str "Appointment  #" appointmentId " has been booked")])])]]))

(def part "Spark Plug")


(defn select-appointment [event]
  (let  [appointments @(rf/subscribe [:appointments])]
    [:div
     (for [a appointments]
       (let [{appointmentId :appointmentId color :color year :year make :make model :model vehicleId :vehicleId} a]
         [:div {:style {:color "blue" :cursor "pointer"} :on-click  #(rf/dispatch [event appointmentId])}
          [:span {:style {:margin-right "20px"}} (str "#" appointmentId)]
          [:span (str " " color " " year " " make " " model " ")]]))]))

(defn update-page []
  (let [update-tasks @(rf/subscribe [:update/updateTasks])
        test-tasks (:tests update-tasks)
        replacement-tasks (:partReplacements update-tasks)
        task-success @(rf/subscribe [:update/task-success])
        selected-task @(rf/subscribe [:update/selected-task])
        successful @(rf/subscribe [:update/update-successful])
        testStatus @(rf/subscribe [:update/test-status])]
    [:section.section>div.container>div.content
     [:h1 "Update Service Record"]
     [:div
      [:h5 "Which Appointment Should Be Updated?"]
      [select-appointment :get-appointment-tasks]
      [:br]
      (when task-success
        [:div
         [:h5 "Select a Task to Update"]
         [:div
          [:h6 "Part Replacements"]
          [:div (for [{:keys [taskId taskName partId partName costOfPart ]} replacement-tasks]
                  [:div
                   [:div {:on-click #(rf/dispatch [:update-update-selected-task taskId]) :style {:color "blue" :cursor "pointer"}} taskName]
                   (when (= taskId selected-task)
                     [:div
                      [:div
                       {:on-click #(rf/dispatch [:add-part-to-bill partId taskId]) :style {:color "blue" :cursor "pointer"}}
                       (str  "Add " partName " - $" costOfPart " -" " to Bill, and Mark Replacement Complete?")]
                      (when successful
                        [:h6 (str partName " has been added to the bill and " taskName " has been marked as complete")])])])]]
         [:br]
         [:div
          [:h6 "Tests"]
          [:div (for [{:keys [taskId taskName testFailureTaskId testFailureTaskName]} test-tasks]
                  [:div
                   [:div {:on-click #(rf/dispatch [:update-update-selected-task taskId]) :style {:color "blue" :cursor "pointer"}} taskName]
                   (when (= taskId selected-task)
                     [:div
                      [:h6 {:style {:margin-top "10px" :margin-bottom "0px"}} "Did the test Pass?"]
                      [:div
                       [:div {:on-click #(rf/dispatch [:update-test-status "Passed" taskId]) :style {:color "blue" :cursor "pointer"}} "Yes"]
                       (when (and successful (= testStatus "Passed"))
                         [:div
                          [:h6 (str taskName " has been marked as complete")]])]
                      [:div
                       [:div {:on-click #(rf/dispatch [:update-test-status "Failed" testFailureTaskId]) :style {:color "blue" :cursor "pointer"}} "No"]
                       (when (and successful (= testStatus "Failed"))
                         [:div
                          [:h6 (str testFailureTaskName " has been added to the list of tasks to do and " taskName " has been marked as complete")]])]
                      [:br]])])]]])]]))

(defn bill-page []
  [:section.section>div.container>div.content
   [:h1 "Bill"]
   [:div
    [:h5 "Which Appointment Has Ended?"]
    [select-appointment :tbd]
    [:div {:style (assoc (:button styles) :margin-top "20px" :margin-bottom "20px")} "End Appointment and Create Bill"]
    ;; TODO Make fucking bill!!!
    [:div "BILL"]]])

;;Not sure exactly how to handle arrival, not sure what is expected
(defn arrival-page []
  (let [{:keys [appointmentId success]} @(rf/subscribe [:dropoff])]
    [:section.section>div.container>div.content
     [:h1 "Arrival"]
     [:div
      [:h5 "Which Appointment Has Arrived?"]
      [select-appointment :dropoff-car]
      (when success
        [:h5 {:style {:margin-top "20px"}} (str "Appointment #" appointmentId " has been marked as \"Dropped Off\"")])]]))

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
