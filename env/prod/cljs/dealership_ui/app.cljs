(ns dealership-ui.app
  (:require [dealership-ui.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
