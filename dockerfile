# syntax=docker/dockerfile:1
FROM openjdk:17
ENV SERIALNUMBER=SERIALNUMBER
ENV MQTTBROKERHOST=MQTTBROKERHOST
ENV MQTTBROKERPORT=MQTTBROKERPORT
ENV USERNAME=USERNAME
ENV PASSWORD=PASSWORD
ENV INTERVAL=INTERVAL
ADD target/VaillantMqttClient.jar /home/VaillantMqttClient.jar
CMD ["java","-jar","/home/VaillantMqttClient.jar", "-serialNumber", "${SERIALNUMBER}", "-mqttBrokerHost", "${MQTTBROKERHOST}", "-mqttBrokerPort", "${MQTTBROKERPORT}", "-username", "${USERNAME}", "-password", "${PASSWORD}", "-interval", "${INTERVAL}"]