(ns dealership-ui.events
  (:require
    [dealership-ui.db :as db]
    [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub reg-fx]]
    [ajax.core :as ajax]
    [reitit.frontend.easy :as rfe]
    [reitit.frontend.controllers :as rfc]))

;;dispatchers

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

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

;;subscriptions

(reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

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
  :common/page-id
  :<- [:common/route]
  (fn [route _]
    (-> route :data :name)))

(reg-sub
  :common/page
  :<- [:common/route]
  (fn [route _]
    (-> route :data :view)))

