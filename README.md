# oradcn-demo

This is a simple JDBC demo of the Oracle Database Change Notification
feature, implemented in Clojure using clojure.java.jdbc and other
auxiliary packages.

It's a more or less direct convertion of the online Java DCN examples,
and it's not likely to be very "idiomatic" since my knowledge of
Clojure is superficial at best.

After passing some command line arguments (used to establish the
database connection) this program will wait for notification and
simply print the notification event to standard output.


## Installation

Download from https://github.com/fsmunoz/oradcn-demo, the demo uses
leiningen so should be trivial.

One import point is that the ojdbc6.jar file (the Oracle Thin JDBC
driver) is not distributed as part of this demo and must be obtained
from Oracle (or a locally installed copy used). The location of the
jar must be changed in the project.clj file. 

For development Oracle 11.2 XE was used, including the built-in demo tables.

## Usage

Simply launch it via lein as per the example below

## Options

      Switches               Default                       Desc                                              
      --------               -------                       ----                                              
      -h, --host                                           Database host                                     
      -p, --port             1521                          The port to connect to                            
      -u, --user                                           Username                                          
      -p, --password                                       Password                                          
      -d, --db                                             Database                                          
      -l, --local-port       6666                          The local port that will be used for the callback 
      -q, --query            select * from DEMO_CUSTOMERS  The query used for registration                   
      -h, --no-help, --help  false                         Show help                                         


## Examples

     $ lein run -- --user fsmunoz --password p4ss --db XE --host aixdev  --local-port 7777 -q "select ENAME from EMP"
     > Connecting...  connected.
     > Registered Tables:  [FSMUNOZ.EMP]
     > DCN Registration ID:  701
     > Waiting for DCN events...
     
     Connection information  : local=192.168.122.1/192.168.122.1:7777, remote=aixdev/192.168.122.179:54157
     Registration ID         : 701
     Notification version    : 1
     Event type              : QUERYCHANGE
     Database name           : XE
     Query Change Description (length=1)
       query ID=82, query change event type=QUERYCHANGE
       Table Change Description (length=1):    operation=[UPDATE], tableName=FSMUNOZ.EMP, objectNumber=20043
     
     ^C
     $



## License

Copyright © 2013 Frederico Munoz

Distributed under the Eclipse Public License, the same as Clojure.
