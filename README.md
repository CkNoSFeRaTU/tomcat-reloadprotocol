## Automatic reloading of SSL Host configuration for Tomcat 9.0.x

Most common use is ability to automatically reload to renewed certificates.

### Installation
Drop **reloadprotocol-x.x.jar** to **lib** directory of your tomcat and change your connector in **conf/server.xml** to something like this:
```
    <Connector port="443" protocol="org.apache.reloadprotocol.Http11NioReloadProtocol"
               maxThreads="150" SSLEnabled="true" reloadInterval="3600">
```

**reloadInterval** is the interval for reloading. Default is 3600 (1 hour) if not specified.
