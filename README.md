# Sniffer-MQTT

## Getting Started
### Prerequisites

* **JDK** - Java Development Kit

* **Ant** - Java compiler used to compile sniffer project. [Linux Instalation](https://docs.wso2.com/display/ESB450/Installing+Apache+Ant+on+Linux) or [Windows Instalation](https://www.mkyong.com/ant/how-to-install-apache-ant-on-windows/)

### Installing

Command to build the sniffer
```
ant -Dnb.internal.action.name=rebuild clean jar
```

Command to run the sniffer
```
java -jar dist/Sniffer.jar
```

## Usage

### Architecture

### MQTT Topics

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
