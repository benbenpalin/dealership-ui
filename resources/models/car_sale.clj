(ns models.car-sale)

;// car sale API
;complete purchase - POST
; creates purchase in purchase table
; update: marks car as not in inventory
; returns bill info (not currently in store)
(def complete-purchase-request
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
   :car-id ""
   :sale-price ""})

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

(def submit-appointment-response
  {:appointment-id})
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
   :vehicle-id ""})

;;; Get appointment tasks - GET
;; get tasks for selected appointment, and related info, from Scheduled table
(def get-appointment-tasks-request
  {:appointment-id ""})

(def get-appointment-tasks-response
  {:tests [{:task-id ""
            :task-name ""
            :test-failure-task-id ""
            :test-failure-task-name ""}]
   :part-replacement [{:task-id ""
                       :task-name ""
                       :part-id ""
                       :part-name ""
                       :cost-of-part ""}]})



;;; Add Part to Bill - POST
;; Adds Parts to Was_Replaced
;; Should also call mark-task-performed
(def add-part-to-bill-request
  {:appointment-id ""
   :part-id ""})

(def add-part-to-bill-response 200)

;;; Add task to required - POST
;; Add replacement_part task to additionally_scheduled
;; Should also call mark-task-performed
(def add-task-request
  {:task-id ""
   :appointment-id ""})

(def add-task-response 200)

;;; confirm task is performed - POST
;;
(def mark-task-performed-request
  {:task-id ""
   :appointment-id ""})

(def mark-task-performed-response 200)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Complete appointment and print bill
;;; print service bill - POST
;; Get bill data, add pick-up timestamp to Appointment
(def print-service-bill-request
  {:appointment-id ""
   :pick-up-time ""})

(def print-service-bill-response
  ;; car info already in store
  {:customer-names [" " " "]
   :tests [{:task-name ""
            :time-to-complete ""
            :labor-cost ""
            :test-status ""}]
   :replacements [{:task-name ""
                   :time-to-complete ""
                   :labor-cost ""
                   :part-name ""
                   :cost-of-part ""}]
   :drop-off ""
   :pick-up ""})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Arrival
;;; appointment-dropped-off - POST
;; Add drop-off time to appointment
(def appointment-dropped-off-request
  {:appointment-id ""})

(def appointment-dropped-off-response 200)
