package com.oring.smartcity.example

import java.io.{File, FileInputStream}

import com.oring.smartcity.makka.{Data, Job}
import org.apache.kafka.clients.producer.{Callback, ProducerRecord, RecordMetadata}
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttMessage}
import play.api.libs.json.Json

/**
  * Created by WeiChen on 2016/6/17.
  */
class MqttSubJob extends Job{
  override def init(): Unit = {
    val in = new FileInputStream(new File("src/main/resources/mqttConf.json"))
    val rawJson = Json.parse(in)
    val topic = (rawJson \ "topic").as[String]
    val mqttBroker = (rawJson \ "broker").as[String]
    println(mqttBroker)
    println(topic)
    val mqttClient = new MqttClient(mqttBroker, MqttClient.generateClientId())
    mqttClient.connect()
    mqttClient.subscribe(topic)
    val callback = new MqttCallback {
      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        val jsonStr = "{\"topic\":\"%s\",\"message\":\"%s\"}".format(topic,message)
        val responseData = new Data(jsonStr)
        pipe("KafkaPubJob", responseData)
      }
      override def connectionLost(cause: Throwable): Unit = {
        println(cause)
      }
      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
      }
    }
    mqttClient.setCallback(callback)
  }

  override def receiveData(d: String): Unit = {
  }
}
