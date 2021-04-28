(ns models.car-sale)

;// car sale API
;// create customer - POST
; creates a new customer in the Customer table
(def create-customer-request
  {:first-name ""
   :middle-initial ""
   :last-name ""
   :phone-number ""
   :street-address ""
   :city ""
   :state ""
   :zipcode ""})

(def create-customer-response
  {:customer-id ""})

;complete purchase - POST
; creates purchase in purchase table
; update: marks car as not in inventory
; returns bill info (not currently in store)
(def complete-purchase-request
  {:customer-ids ["", ""]
   :car-id "" ;already in system, so this is all we need
   :sale-price ""
   :date-of-sale ""})

(def complete-purchase-response
  ;;Bill
  {:customer-names ["", ""]
   :purchase-id ""
   ;:date-of-sale "" ;; already in store
   ;:sale-price "" ;; already in store
   :make ""
   :model ""
   :year ""
   :color ""
   :license-plate-number ""
   :license-plate-state ""})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; report api
;; get report - GET
;; no updates or insertions
; pulls data
(def get-report-request
  {:start-date ""
   :end-date ""})

;; list of ALL vehicles in Vehicle_Type table, and related sales data
(def get-report-response
  [{:vehicle-id ""
    :make ""
    :model ""
    :year ""
    :total-sold 100
    :profit 100.00}])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Book Service Appointment Api

;;; Get Vehicle Types and packages ON LOAD- GET
;; pulls all packages
(def get-packages-request nil)

(def get-packages-response
   :packages [{:name "" :package_id ""}])

;;; Get Vehicle Types (on car not in system) - GET
;; pulls (ordered) the makes and models  and years in Vehicle_Type table
(def get-vehicle-types-request nil)

(def get-vehicle-types-response
 [{:make ""
   :model ""
   :year ""
   :vehicle-id ""}])


;;; get tasks - GET
;; gets tasks in package, and tasks not in that package
;; If no package ID, return tasks-in-package as nil and all tasks in :tasks-not-in-package
(def get-tasks-request
  {:package-id ""})

(def get-tasks-response
  {:tasks-in-package [{:task_id ""
                       :task-name ""
                       :estd-time}]
   :tasks-not-in-package [{:task_id ""
                           :task-name ""
                           :estd-time}]})

;;; get timeslot - GET
;; Total estimated time of selected tasks, and get timeslots that work
(def get-timeslot-request
  {:total-time 10
   :date ""})

(def get-timeslot-response
  [{:timeslot-id ""
    :start-time ""
    :end-time ""}])

;;; Submit appointment - POST
;; adds new customer(s) (if applicable)
;; adds new car (if applicable)
  ;; insert entry in Owns table
;; Create appointment in Appointment table,
;; add all tasks to Additionally scheduled.
(def submit-appointment-request
  {:customer {:new true
              :customer-ids [""];;optional, only if false
              :new-customers [{:first-name "";;optional, only if true
                               :middle-initial ""
                               :last-name ""
                               :phone-number ""
                               :street-address ""
                               :city ""
                               :state ""
                               :zipcode ""}]}
   :car {:new true
         :car-id "" ;;optional, only if false
         :vehicle-id" ";;optional, only if true
         :license-plate-number "";;optional, only if true
         :license-plate-state "";;optional, only if true
         :color "";;optional, only if true
         :odometer ""};;optional, only if true
   :package-id ""
   :tasks ["task-id" "task-id"]
   :timeslot-id ""})

(def submit-appointment-response 200)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Update Service Record
;;; get today's appointments ON LOAD - GET
(def todays-appointments-request nil)

(def todays-appointments-response
  {:appointment-id ""
   :color ""
   :year ""
   :make ""
   :model ""
   :vehicle-id})

;;; Get appointment tasks - GET
;; get tasks for selected appointment, from Scheduled table
(def get-appointment-tasks-request
  {:appointment-id ""})

(def get-appointment-tasks-response
  [{:task-id ""
    :task-name ""
    :task-type ""}])

;;;get part and price - GET
;; pulls part for part replacement from task and vehicle_id
(def get-part-and-price-request
 {:task-id ""
  :car-id ""})

(def get-part-and-price-response
  {:part-id ""
   :part-name ""
   :cost-of-part ""})

;;; Add Part to Bill - POST
;; Adds Parts to was replaced
(def add-part-to-bill
  {:})
;;;TODO CONSOLIDATE AS MANY CALLS AS YOU CAN
;;; get test failure - GET
;; Pull what is required after a failure from a given test
(def get-test-failure-request
  {:task-id ""})

(def get-test-failure-response
  [{:task-id "" :task-name ""}])










