# syntax=docker/dockerfile:1
FROM openjdk:17
ARG SERIALNUMBER=SERIALNUMBER
ARG MQTTBROKERHOST=MQTTBROKERHOST
ARG MQTTBROKERPORT=MQTTBROKERPORT
ARG USERNAME=USERNAME
ARG PASSWORD=PASSWORD
ARG INTERVAL=INTERVAL
ADD target/VaillantMqttClient.jar /home/VaillantMqttClient.jar
CMD ["java","-jar","/home/VaillantMqttClient.jar", "-serialNumber", "${SERIALNUMBER}", "-mqttBrokerHost", "${MQTTBROKERHOST}", "-mqttBrokerPort", "${MQTTBROKERPORT}", "-username", "${USERNAME}", "-password", "${PASSWORD}", "-interval", "${INTERVAL}"]