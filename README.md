# MQTT-RD Sniffer

### Prerequisites

* **JDK** - Java Development Kit

* **Ant** - Java compiler used to compile sniffer project. [Linux Instalation](https://docs.wso2.com/display/ESB450/Installing+Apache+Ant+on+Linux) or [Windows Instalation](https://www.mkyong.com/ant/how-to-install-apache-ant-on-windows/)

### Installing

After clonning the repository, in the folder of the project, you need to build the sniffer with the following command:
```
ant -Dnb.internal.action.name=rebuild clean jar
```

After building the jar you can run the program with:
```
java -jar dist/Sniffer.jar
```

### Sniffer Configuration File

In the path [/src/com/sniffer/resources/config1.json](/src/com/sniffer/resources/config1.json) you can find the configuration file were you can change the configuration settings.

The configuration settings that you can change are:

* ***sniffer_id*** - unnique id that you want to use.

* ***sniffer_type*** - local or internet (if the broker was in a internet address).

* ***start_broker*** - if you want to start automatically a broker (recomended yes).

* ***remote broker settings*** - IP address, port, username and password.

* ***local broker settings*** - 'autodetected' if you want ta the Sniffer detects the broker automatically, otherwise you must specify the IP. You need also to define the port.

* ***network settings*** - Multicast address that you want to use for the device/sniffer discovery. You need also to define the port and the network id.

## Device Usage

If your device doesn´t know the IP of the Sniffer, you can discovert sending a multicast package to the multicast IP 224.0.1.1 and the port 9876, with the message 'newdevice'.

This way, the device needs to communicate with the Sniffer using multicast sockets (discovering the IP address) ([example file](/src/com/sniffer/udp/CheckNetworkThread.java)) and over MQTT (normal communication) ([example files](/src/com/sniffer/mqtt)). 

### MQTT Topics

The topics that the new devices uses for the the communication with the Sniffer are the following:

<!-- Topic to regist a public entity (sniffer) or a new device in the others entities (sniffers). -->

Topic to regist a new device in the sniffer responsible for the local network.
```
/registdevice
```  

Topic to get the list off entities and devices from the Sniffer-MQTT.
```
/getlist
```
  
<!-- Topic that devices use to get all topics that they can subscribe. -->
  
Topic used by the device, to publish the data from one attribute.
```
/device_id/attrs/attribute_id
```

**device_id:** ID used to register the device in the platform.  
**attribute_id:** ID used to register the attribute of that device in the platform.
