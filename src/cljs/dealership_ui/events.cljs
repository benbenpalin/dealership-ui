(ns dealership-ui.events
  (:require
    [dealership-ui.db :as db]
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub reg-fx]]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;; Helpers

(defn url [end-point]
  (str "http://localhost:4567" end-point))

;;dispatchers

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))


(reg-event-db
  :update-packages
  (fn [db [_ packages]]
    (merge db packages)))

(reg-event-fx
  :get-packages-on-load
  (fn
    [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             (url "/api/packages")
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:update-packages]
                  :on-failure      [:failed-report]}}))

(reg-event-db
  :update-appointments
  (fn [db [_ appointments]]
    (merge db appointments)))

(reg-event-fx
  :get-appointments-on-load
  (fn
    [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             (url "/api/appointments")
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:update-appointments]
                  :on-failure      [:failed-report]}}))

(reg-event-db
  :update-vehicle-types
  (fn [db [_ vehicle-types]]
    (merge db vehicle-types)))

(reg-event-fx
  :get-vehicle-types-on-load
  (fn
    [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             (url "/api/vehicletypes")
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:update-vehicle-types]
                  :on-failure      [:failed-report]}}))

;; Nav

(reg-event-db
  :common/navigate
  (fn [db [_ match]]
    (let [old-match (:common/route db)
          new-match (assoc match :controllers
                                 (rfc/apply-controllers (:controllers old-match) match))]
      (assoc db :common/route new-match))))

(reg-fx
  :common/navigate-fx!
  (fn [[k & [params query]]]
    (rfe/push-state k params query)))

(reg-event-fx
  :common/navigate!
  (fn [_ [_ url-key params query]]
    {:common/navigate-fx! [url-key params query]}))

;; Car Sale
(reg-event-db
  :change-number-of-customers
  (fn [db [_ number]]
    (assoc-in db [:sale :number-of-customers] number)))

(reg-event-db
  :change-sale-customer-status
  (fn [db [_ status]]
    (assoc-in db [:sale :customer-status] status)))

;; Book Service
(reg-event-db
  :change-book-customer-status
  (fn [db [_ status]]
    (assoc-in db [:book :customer-status] status)))

(reg-event-db
  :change-book-number-of-customers
  (fn [db [_ status]]
    (assoc-in db [:book :number-of-customers] status)))

(reg-event-db
  :change-book-car-status
  (fn [db [_ status]]
    (assoc-in db [:book :car-status] status)))

(reg-event-db
  :update-book-customer-1-value
  (fn [db [_ k v]]
    (assoc-in db [:book :customer :newCustomer1 k] v)))

(reg-event-db
  :update-book-customer-2-value
  (fn [db [_ k v]]
    (assoc-in db [:book :customer :newCustomer2 k] v)))

(reg-event-db
  :update-book-vehicle-id
  (fn [db [_ v]]
    (assoc-in db [:book :car :vehicleId] v)))

(reg-event-db
  :update-book-car-value
  (fn [db [_ k v]]
    (assoc-in db [:book :car k] v)))

(reg-event-fx
  :change-package
  (fn [{:keys [db]} [_ packageId]]
    {:db (assoc-in db [:book :package] packageId)
     :dispatch [:get-package-tasks packageId]}))

(reg-event-db
  :update-check
  (fn [db [_ taskPackageRelation taskId prev-checked]]
    (update-in db [:packageTasks taskPackageRelation]
               (fn [v]
                 (map #(if (= (:taskId %) taskId) (assoc % :checked (not prev-checked)) %) v)))))

(reg-event-db
  :update-date-of-service
  (fn [db [_ date]]
    (assoc-in db [:book :date-of-service] date)))

(reg-event-db
  :update-timeslots
  (fn [db [_  timeslots]]
    (assoc-in db [:book :timeslots] (:timeslots timeslots))))

(reg-event-fx
  :get-timeslots
  (fn
    [{:keys [db]} _]
    (let [{:keys [inPackage notInPackage]} (:packageTasks db)
          all-tasks (concat inPackage notInPackage)
          totalTime (reduce #(+ (:estdTime %2) %1) 0 all-tasks)]
      {:http-xhrio {:method          :get
                    :uri             (url "/api/timeslots")
                    :params {:date (-> db :book :date-of-service)
                             :totalTime totalTime}
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:update-timeslots]
                    :on-failure      [:failed-report]}})))

(reg-event-db
  :change-selected-timeslot
  (fn [db [_  timeslotId]]
    (assoc-in db [:book :timeslotId] timeslotId)))

;;;

(reg-event-db
  :update-sale-customer-1-value
  (fn [db [_ k v]]
    (assoc-in db [:sale :customer :newCustomer1 k] v)))

(reg-event-db
  :update-sale-customer-2-value
  (fn [db [_ k v]]
    (assoc-in db [:sale :customer :newCustomer2 k] v)))

(reg-event-db
  :update-sale-customer-val
  (fn [db [_ k v]]
    (assoc-in db [:sale :customer k] v)))

(reg-event-db
  :update-sale-val
  (fn [db [_ k v]]
    (assoc-in db [:sale k] v)))

(reg-event-db
  :update-sale-bill
  (fn [db [_ bill]]
    (assoc-in db [:sale :bill] bill)))


(reg-event-fx
  :submit-purchase
  (fn
    [{:keys [db]} _]
    (let [sale  (:sale db)
          customer (:customer sale)
          isNew  (= (:customer-status customer) "new")]
      {:http-xhrio {:method          :post
                    :uri             (url "/api/purchase")
                    :params          {:customer {:isNew  isNew
                                                 :customerIds (if isNew
                                                                []
                                                                [(:customerId1 customer) (:customerId2 customer)])
                                                 :newCustomers (if isNew
                                                                 [(:newCustomer1 customer) (:newCustomer2 customer)]
                                                                 [])}
                                      :carId (:carId sale)
                                      :salePrice (:salePrice sale)}
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:update-sale-bill]
                    :on-failure      [:failed-report]}})))

;;;

(reg-event-fx
  :get-package-tasks
  (fn
    [{:keys [db]} [_ packageId]]
    {:http-xhrio {:method          :get
                  :uri             (url "/api/tasksinpackage")
                  :params {:packageId packageId}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:update-package-tasks]
                  :on-failure      [:failed-report]}}))

(reg-event-db
  :update-package-tasks
  (fn [db [_ result]]
     (let [new-result (update result :inPackage (fn [m] (map #(assoc % :checked true) m)))
           final-result (update new-result :notInPackage (fn [m] (map #(assoc % :checked false) m)))]
      (assoc db :packageTasks (assoc final-result :loaded true)))))

;; Report
(reg-event-db
  :set-start-date
  (fn [db [_ start]]
    (assoc-in db [:sales-report :start-date] start)))

(reg-event-db
  :set-end-date
  (fn [db [_ end]]
    (assoc-in db [:sales-report :end-date] end)))

(reg-event-db
  :update-report
  (fn [db [_ result]]
    (assoc-in db [:sales-report :report] [result])))

(reg-event-db
  :failed-report
  (fn [db _] db))

(reg-event-fx
  :in-between
  (fn [{:keys [db]} _]
    {:dispatch [:pull-report]}))


(reg-event-fx
  :pull-report
  (fn
    [{:keys [db]} _]
    (let [start (get-in db [:sales-report :start-date])
          end (get-in db [:sales-report :end-date])]
      {:http-xhrio {:method          :get
                    :uri             (url "/api/report")
                    :params {:startDate start
                             :endDate end}
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:update-report]
                    :on-failure      [:failed-report]}})))

;;; Update Service Record
(reg-event-fx
  :get-appointment-tasks
  (fn
    [{:keys [db]} [appointmentId _]]
    {:http-xhrio {:method          :get
                  :uri             (url "/api/appointmenttasks")
                  :params {:appointmentId appointmentId}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:update-update-tasks]
                  :on-failure      [:failed-report]}}))

(reg-event-db
  :update-update-tasks
  (fn [db [_ result]]
    (assoc db :updateTasks result)))

;;; dropoff
(reg-event-db
  :dropoff-success
  (fn [db _]
    (assoc-in db [:dropoff :success] true)))

(reg-event-fx
  :dropoff-car
  (fn
    [{:keys [db]} [_ appointmentId]]
    {:db (assoc db :dopoph appointmentId)
     :http-xhrio {:method          :post
                  :uri             (url "/api/dropoff")
                  :params {:appointmentId appointmentId}
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:update-report]
                  :on-failure      [:failed-report]}}))


;;subscriptions

(reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

(reg-sub
  :sale/number-of-customers
  (fn [db _]
    (-> db :sale :number-of-customers)))

(reg-sub
  :sale/bill
  (fn [db _]
    (-> db :sale :bill)))

(reg-sub
  :sale/salePrice
  (fn [db _]
    (-> db :sale :salePrice)))

(reg-sub
  :packageTasks
  (fn [db _]
    (-> db :packageTasks)))

(reg-sub
  :packages
  (fn [db _]
    (-> db :packages)))

(reg-sub
  :appointments
  (fn [db _]
    (-> db :appointments)))

(reg-sub
  :vehicleTypes
  (fn [db _]
    (-> db :vehicleTypes)))

(reg-sub
  :sale/customer-status
  (fn [db _]
    (-> db :sale :customer-status)))

(reg-sub
  :book/customer-status
  (fn [db _]
    (-> db :book :customer-status)))

(reg-sub
  :book/timeslots
  (fn [db _]
    (-> db :book :timeslots)))

(reg-sub
  :book/car-status
  (fn [db _]
    (-> db :book :car-status)))

(reg-sub
  :book/:number-of-customers
  (fn [db _]
    (-> db :book :number-of-customers)))

(reg-sub
  :book/package
  (fn [db _]
    (-> db :book :package)))

(reg-sub
  :sales-report/report
  (fn [db _]
    (-> db :sales-report :report)))

(reg-sub
  :sales-report/start-date
  (fn [db _]
    (-> db :sales-report :start-date)))

(reg-sub
  :sales-report/end-date
  (fn [db _]
    (-> db :sales-report :end-date)))

(reg-sub
  :updateTasks
  (fn [db _]
    (-> db :updateTasks)))
;;;;;;;;;;;;;;;;;;;;;

(reg-sub
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

