(ns dealership-ui.db)

(def tester
  {:inPackage    [{:taskId 1
                   :taskName "Brake Test"
                   :estdTime 1
                   :checked true}
                  {:taskId 2
                   :taskName "Alternator Test"
                   :estdTime 2
                   :checked false}]
   :notInPackage [{:taskId 1
                   :taskName "Brake Test"
                   :estdTime 1}
                  {:taskId 2
                   :taskName "Alternator Test"
                   :estdTime 2}]})


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
            :number-of-customers "one"
            :customer {:newCustomer1 {:firstName "";;optional, only if true
                                      :middleInitial ""
                                      :lastName ""
                                      :phoneNumber ""
                                      :streetAddress ""
                                      :city ""
                                      :state ""
                                      :zipcode ""}}
            :car-status "existing"
            :car {:carId 0
                  :vehicleId 0
                  :licensePlateNumber ""
                  :licensePlateState ""
                  :color ""
                  :odometer ""}
            :packageId 0
            :tasks []
            :timeslotId 0}

     :sales-report {}
     :dropOff {:appointmentId "23456"
               :success false}
     :updateTasks {:appointmentId ""}


     :packages [{}]
     :appointments [{}]
     :vehicleTypes [{}]}))




