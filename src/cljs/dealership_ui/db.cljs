(ns dealership-ui.db)

(def default-db
  (def default-db
    {:sale {:customer-status "existing"}
     :book {:customer-status "existing"
            :car-status "existing"
            :package "No Package"}
     :sales-report {:start-date nil
                    :end-date nil
                    :report [{:vehicleId "1234"
                              :make "Subaru"
                              :model "Forest"
                              :year "2015"
                              :totalSold 100
                              :profit 1000000.00}]}
     :dropOff {:appointmentId "23456"
               :success false}
     :packages [{}]
     :appointments [{}]
     :vehicleTypes [{}]}))




