(ns oradcn-demo.core
  (:gen-class)
  (:require [clojure.java.jdbc :as j]
            [clojure.java.jdbc.sql :as s]
            [clojure.contrib.command-line :as ccl])
  (:use [clojure.tools.cli :only [cli]])
  (:import java.util.Properties
           oracle.jdbc.OracleConnection
           oracle.jdbc.dcn.DatabaseChangeEvent
           oracle.jdbc.dcn.DatabaseChangeListener
           oracle.jdbc.dcn.DatabaseChangeRegistration
           oracle.jdbc.OracleStatement
           java.util.Arrays))

(defn -main
  ;;    "Oracle Database Change Notification JDBC demo"
  [& args]
  ;; work around dangerous default behaviour in Clojure (NB: this comment is from leiningen)
  (alter-var-root #'*read-eval* (constantly false))
  
  (let [[options args banner] (cli args
                                   ["-h" "--host"  "Database host"]
                                   ["-p" "--port" "The port to connect to" :default 1521 :parse-fn #(Integer. %)]
                                   ["-u" "--user"  "Username"]
                                   ["-p" "--password" "Password"]
                                   ["-d" "--db" "Database"]
                                   ["-l" "--local-port" "The local port that will be used for the callback" :default 6666]
                                   ["-q" "--query" "The query used for registration" :default "select * from DEMO_CUSTOMERS"]
                                   ["-h" "--help" "Show help" :default false :flag true])
        ;; DB Connection spec
        db {:classname "oracle.jdbc.driver.OracleDriver" ;must be in the path
            :subprotocol "oracle"
            :subname (str "thin:@" (:host options) ":" (:port options) ":" (:db options) )
            :user (:user options)
            :password (:password options)}
        prop (new Properties)]   ; Holds the connection properties
    

    (when (:help options)  ; Help handling
      (println banner)
      (System/exit 0))
    
    ;; Obtain connection, with some debug output
    (print "> Connecting... ")
    (try 
      (do
        (def conn (j/get-connection db))
        (println " connected."))
      (catch Exception e (do
                           (println "Connection failed")
                           (println e)
                           (System/exit 1))))
    
    ;; Set the connection options, feel free to play around with the hardcoded ones
    (doto prop
      (.setProperty OracleConnection/DCN_NOTIFY_ROWIDS "true")
      (.setProperty OracleConnection/DCN_QUERY_CHANGE_NOTIFICATION "true")
      (.setProperty OracleConnection/NTF_LOCAL_TCP_PORT (:local-port options)))
    
    ;; Create the notification register with the correct option on the right connection
    ;; For this notify priviledges are needed, otherwise SQLException ORA-29972
    (def dcr (.registerDatabaseChangeNotification conn prop))
    
    
    ;; Add the listener - using reify but proxy works as well of course
    (.addListener dcr
                  (reify DatabaseChangeListener
                    (onDatabaseChangeNotification [_ evt]
                      (println (.toString evt)))))
    
    (def stmt (.createStatement conn)) ; create a new statement
    (.setDatabaseChangeRegistration stmt dcr) ; add the registration to it 
    
    (def rs (.executeQuery stmt (:query options))) ; specify the tables by using a query
    (while (.next rs)) ; Loop through the results - all examples do this, although it works without it. Leaving it as black magic.
    
    (.close rs)
    (.close stmt)
    
    ;; Some more debug output
    (println "> Registered Tables: " (Arrays/toString (.getTables dcr)))
    (println "> DCN Registration ID: " (.getRegistrationId dcr))
    (println "> Waiting for DCN events...")
    
    ;; The main thread only exits after the JDBC connection is closed, so no need for a run loop
    ;; It will keep on listening until Control-C is pressed 
    
    ;; Close the registration on interrupt
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. (fn [] 
                                 (print "Shutting down...")
                                 (.unregisterDatabaseChangeNotification conn dcr)
                                 (.close conn))))))
