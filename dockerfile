# syntax=docker/dockerfile:1
FROM openjdk:17
ADD target/VaillantMqttClient-1.0.jar /home/VaillantMqttClient-1.0.jar
CMD ["java","-jar","/home/VaillantMqttClient.jar", "-serialNumber $SERIALNUMBER", "-mqttBrokerHost $MQTTBROKERHOST", "-mqttBrokerPort $MQTTBROKERPORT", "-username $USERNAME", "-password $PASSWORD", "-interval $INTERVAL"]