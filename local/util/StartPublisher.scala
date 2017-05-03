package com.oring.smartcity.util

import dao._
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
  * Created by janux on 2016/7/1.
  */

object StartPublisher {
  def main(args: Array[String]) {
    publish()
  }

  def publish(): Unit = {
    var client: MqttClient = null
    val persistence = new MemoryPersistence

    try {
      // mqtt client with specific url and client id
      client = new MqttClient("ssl://140.119.19.243:8883", MqttClient.generateClientId, persistence)

      val options = new MqttConnectOptions
      val sslContext = new SslUtil().getSslContext("mytest", getClass().getResourceAsStream("/cacerts"))

      options.setSocketFactory(sslContext.getSocketFactory)
      options.setUserName("test")
      options.setPassword("test".toCharArray())
      client.connect(options)
      println(client.isConnected())

      val publishTopic = "/StartCovCor"
      val msg = "start cov!"

      val msgTopic = client.getTopic(publishTopic)
      val message = new MqttMessage(msg.getBytes("utf-8"))
      msgTopic.publish(message)
      println("Publishing Data, Topic : %s, Message : %s".format(msgTopic.getName, message))
    }

    catch {
      case e: MqttException => println("Exception Caught: " + e)
    }

    finally {
      client.disconnect()
    }
  }
}