(ns dealership-ui.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [dealership-ui.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[dealership-ui started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[dealership-ui has shut down successfully]=-"))
   :middleware wrap-dev})
