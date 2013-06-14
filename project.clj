(defproject oradcn-demo "0.1.0-SNAPSHOT"
  :description "Oracle Data Change Notification demo using JDBC"
  :url "http://finisterra.motd.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojure-contrib "1.2.0"] 
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 [org.clojure/tools.cli "0.2.2"]]
  :resource-paths ["/home/fsmunoz/src/ojdbc6.jar"] ;; <technomancy> that's not recommended for Real Projects
  :main oradcn-demo.core)

