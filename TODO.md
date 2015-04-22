* Implement event bus metrics (+ example)
* Minimal documentation (+ check Javadoc: indicate the type of metrics)
* Integration tests
* Decouple collection from sending (still create timers for collection but put collected metrics into a queue)
* Do not drop data on sending error
* Make list of servers (http and net) monitored configurable
* Support HTTPS (with configurable trust and key stores)
* How to support different versions of Vert.x 3?
* Add a switch to enable MBeans reports
* Add a switch to disable sending (MBean reports only)
* Add the ability to collect data from MBeans (or is there a better way to collect application level metrics?)

