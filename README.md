# VaillantMqttClient  [![Build Jar an Docker](https://github.com/2110781006/VaillantMqttClient/actions/workflows/build.yml/badge.svg)](https://github.com/2110781006/VaillantMqttClient/actions/workflows/build.yml)

Gets data from vaillant heating/cooling device over the REST-API from vaillant and publish this data to the defined MQTT-Broaker periodicaly.

## Jar-Usage
```console
usage: programm [options]
 -interval          Publish interval in minutes. default: 5 minutes
 -mqttBrokerHost    MQTT broker hostname/ip
 -mqttBrokerPort    MQTT broker port number. default: 1883
 -password          Vaillant password
 -serialNumber      Serial number of the vaillant heating/cooling device
 -username          Vaillant username
```

## Jar-Example
```console
java -jar VaillantMqttClient-1.0.jar -mqttBrokerHost 12.0.0.216 -password xxxxx -username user -serialNumber 12345 -interval 5 -mqttBrokerPort 1987
```

## Docker-Example
```console
TODO
```