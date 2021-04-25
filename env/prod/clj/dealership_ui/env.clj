(ns dealership-ui.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[dealership-ui started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[dealership-ui has shut down successfully]=-"))
   :middleware identity})
