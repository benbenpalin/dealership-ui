(ns dealership-ui.db)

(def default-db
  (def default-db
    {:sale {:customer-status "existing"
            :number-of-customers "one"
            :customer {:customerId1 nil
                       :customerId2 nil
                       :newCustomer1 {:firstName "";;optional, only if true
                                      :middleInitial ""
                                      :lastName ""
                                      :phoneNumber ""
                                      :streetAddress ""
                                      :city ""
                                      :state ""
                                      :zipcode ""}
                       :newCustomer2 {}}}
     :book {:customer-status "existing"
            :car-status "existing"}
     :sales-report {}
     :dropOff {:appointmentId "23456"
               :success false}
     :updateTasks {:appointmentId ""}


     :packages [{}]
     :appointments [{}]
     :vehicleTypes [{}]}))




