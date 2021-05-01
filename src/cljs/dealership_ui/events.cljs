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
  :change-sale-customer-status
  (fn [db [_ status]]
    (assoc-in db [:sale :customer-status] status)))

;; Book Service
(reg-event-db
  :change-book-customer-status
  (fn [db [_ status]]
    (assoc-in db [:book :customer-status] status)))

(reg-event-db
  :change-book-car-status
  (fn [db [_ status]]
    (assoc-in db [:book :car-status] status)))

(reg-event-db
  :change-package
  (fn [db [_ package]]
    (assoc-in db [:book :package] package)))

(reg-event-fx
  :get-packages
  (fn
    [{:keys [db]} _]
    {:http-xhrio {:method          :get
                  :uri             (url "/api/packages")
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:update-report]
                  :on-failure      [:failed-report]}}))

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
  :packages
  (fn [db _]
    (-> db :packages)))

(reg-sub
  :appointments
  (fn [db _]
    (-> db :appointments)))

(reg-sub
  :sale/customer-status
  (fn [db _]
    (-> db :sale :customer-status)))

(reg-sub
  :book/customer-status
  (fn [db _]
    (-> db :book :customer-status)))

(reg-sub
  :book/car-status
  (fn [db _]
    (-> db :book :car-status)))

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

