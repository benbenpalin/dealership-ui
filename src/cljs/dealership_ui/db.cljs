(ns dealership-ui.db)

(def default-db
  (def default-db
    {:sale {:customer-status "existing"}
     :book {:customer-status "existing"
            :car-status "existing"
            :package "No Package"}
     :sales-report {}
     :dropOff {:appointmentId "23456"
               :success false}
     :updateTasks {:appointmentId ""}


     :packages [{}]
     :appointments [{}]
     :vehicleTypes [{}]}))




