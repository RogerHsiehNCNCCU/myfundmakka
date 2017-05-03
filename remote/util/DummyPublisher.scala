package com.oring.smartcity.util

import org.eclipse.paho.client.mqttv3.{MqttClient, MqttException, MqttMessage}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

/**
  * Created by janux on 2016/7/1.
  */

object DummyPublisher {
  def main(args: Array[String]) {
    publish()
  }

  def publish(): Unit = {
    var client: MqttClient = null
    val topic = "/SYSTEM/JOB/REQ"
    val msg = "{\n  \"source\":{\n    \"type\":\"system\"\n  },\n  \"topic\":\"/SYSTEM/JOB/REQ\",\n  \"target\":[\"jm-2e6964e2-b6be-43ac-8bc3-77f7fa8c922b\"],\n  \"requestId\":\"req-21333053-d951-46f2-a8c2-8f70a09c67bf\",\n  \"cmd\":{\n    \"type\":\"config\",\n    \"args\":{\n      \"jobs\": [\n    \"util.MqttJob\",\"profit.ProfitJob\",\"saveportfolio.SaveportfolioJob\",\"portfolio.PortfolioJob\",\n   \"get4433.Get4433Job\"    ]\n    }\n  },\n  \"timestamp\":1467611550096\n}"
    // Creating new persistence for mqtt client
    //val persistence = new MqttDefaultFilePersistence("/tmp")
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

      val msgTopic = client.getTopic(topic)
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