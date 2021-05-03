(ns models.car-sale)

;// car sale API
;complete purchase - POST
; creates purchase in purchase table
; update: marks car as not in inventory
; returns bill info (not currently in store)
(def complete-purchase-request
  {:customer {:isNew true
              :customerIds [""];;optional, only if false
              :newCustomers [{:firstName "";;optional, only if true
                              :middleInitial ""
                              :lastName ""
                              :phoneNumber ""
                              :streetAddress ""
                              :city ""
                              :state ""
                              :zipcode ""}]}
   :carId ""
   :salePrice ""})

(def complete-purchase-response
  ;;Bill
  {:customerNames ["", ""]
   :purchaseId ""
   ;:date-of-sale "" ;; already in store
   ;:sale-price "" ;; already in store
   :make ""
   :model ""
   :year ""
   :color ""
   :licensePlateNumber ""
   :licensePlateState ""})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; report api
;; get report - GET - DONE
;; no updates or insertions
; pulls data
(def get-report-request
  {:startDate ""
   :endDate ""})

;; list of ALL vehicles in Vehicle_Type table, and related sales data
(def get-report-response
  [{:vehicleId ""
    :make ""
    :model ""
    :year ""
    :totalSold 100
    :profit 100.00}])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Book Service Appointment Api

;;; Get packages ON LOAD- GET
;; pulls all packages
(def get-packages-request nil)

(def get-packages-response
   {:packages [{:name "" :packageId ""}]})

;;; Get Vehicle Types (on car not in system) - GET
;; pulls (ordered) the makes and models  and years in Vehicle_Type table
(def get-vehicle-types-request nil)

(def get-vehicle-types-response
 [{:make ""
   :model ""
   :year ""
   :vehicleId ""}])


;;; get tasks - GET
;; gets tasks in package, and tasks not in that package
;; If no package ID, return tasks-in-package as nil and all tasks in :tasks-not-in-package
(def get-tasks-request
  {:packageId ""})

(def get-tasks-response
  {:tasksInPackage [{:taskId ""
                     :taskName ""
                     :estdTime ""}]
   :tasksNotInPackage [{:taskId ""
                        :taskName ""
                        :estdTime ""}]})

;;; get timeslot - GET
;; Total estimated time of selected tasks, and get timeslots that work
(def get-timeslot-request
  {:totalTime 10
   :date ""})

(def get-timeslot-response
  {:timeslots [{:timeslotId ""
                :startTime ""
                :endTime ""}]})

;;; Submit appointment - POST
;; adds new customer(s) (if applicable)
;; adds new car (if applicable)
  ;; insert entry in Owns table
;; Create appointment in Appointment table,
;; add all tasks to Additionally scheduled.
(def submit-appointment-request
  {:customer {:isNew true
              :customerIds [""];;optional, only if false
              :newCustomers [{:firstName "";;optional, only if true
                              :middleInitial ""
                              :lastName ""
                              :phoneNumber ""
                              :streetAddress ""
                              :city ""
                              :state ""
                              :zipcode ""}]}
   :car {:isNew true
         :carId "" ;;optional, only if false
         :vehicleId" ";;optional, only if true
         :licensePlateNumber "";;optional, only if true
         :licensePlateState "";;optional, only if true
         :color "";;optional, only if true
         :odometer ""};;optional, only if true
   :packageId ""
   :tasks ["task-id" "task-id"]
   :timeslotId ""})

(def submit-appointment-response
  {:appointmentId})
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Update Service Record
;;; get today's appointments ON LOAD - GET
(def todays-appointments-request nil)

(def todays-appointments-response
  {:appointments [{:appointmentId ""
                   :color ""
                   :year ""
                   :make ""
                   :model ""
                   :vehicleId ""}]})

;;; Get appointment tasks - GET
;; get tasks for selected appointment, and related info, from Scheduled table
(def get-appointment-tasks-request
  {:appointmentId ""})

(def get-appointment-tasks-response
  {:tests [{:taskId ""
            :taskName ""
            :testFailureTaskId ""
            :testFailureTaskName ""}]
   :partReplacements [{:taskId ""
                       :taskName ""
                       :partId ""
                       :partName ""
                       :costOfPart ""}]})



;;; Add Part to Bill - POST
;; Adds Parts to Was_Replaced
;; Should also call mark-task-performed
(def add-part-to-bill-request
  {:appointmentId ""
   :partId ""})

(def add-part-to-bill-response 200)

;;; Add task to required - POST
;; Add replacement_part task to additionally_scheduled
;; Should also call mark-task-performed
(def add-task-request
  {:taskId ""
   :appointmentId ""})

(def add-task-response 200)

;;; confirm task is performed - POST
;;
(def mark-task-performed-request
  {:taskId ""
   :appointmentId ""})

(def mark-task-performed-response 200)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Complete appointment and print bill
;;; print service bill - POST
;; Get bill data, add pick-up timestamp to Appointment
(def print-service-bill-request
  {:appointmentId ""
   :pickUpTime ""})

(def print-service-bill-response
  ;; car info already in store
  {:customerNames [" " " "]
   :tests [{:taskName ""
            :timeToComplete ""
            :laborCost ""
            :testStatus ""}]
   :replacements [{:taskName ""
                   :timeToComplete ""
                   :laborCost ""
                   :partName ""
                   :costOfPart ""}]
   :dropOff ""
   :pickUp ""})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Arrival
;;; appointment-dropped-off - POST
;; Add drop-off time to appointment
(def appointment-dropped-off-request
  {:appointmentId ""})

(def appointment-dropped-off-response 200)
