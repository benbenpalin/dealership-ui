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

;;subscriptions

(reg-sub
  :common/route
  (fn [db _]
    (-> db :common/route)))

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

