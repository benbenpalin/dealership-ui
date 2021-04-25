(ns dealership-ui.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [dealership-ui.core-test]))

(doo-tests 'dealership-ui.core-test)

