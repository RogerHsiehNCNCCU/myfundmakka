package com.oring.smartcity.util

import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import dao._
/**
  * Created by janux on 2016/7/1.
  */

object DummyPublisher {
  def main(args: Array[String]) {
    publish()
  }

  def publish(): Unit = {
    var client: MqttClient = null
    val persistence = new MemoryPersistence

    try {
      // mqtt client with specific url and client id
      client = new MqttClient("tcp://140.119.19.231:1883", MqttClient.generateClientId, persistence)

      //val options = new MqttConnectOptions
      //val sslContext = new SslUtil().getSslContext("mytest", getClass().getResourceAsStream("/cacerts"))

      //options.setSocketFactory(sslContext.getSocketFactory)
      //options.setUserName("test")
      //options.setPassword("test".toCharArray())
      client.connect()
      println(client.isConnected())

      val mfpDAO = new MongoFundPriceDAO()
      val collectionList: Array[String] = mfpDAO.collectionNameHandler(mfpDAO.mdbe.getCollectionNames)
      val publishTopic = "/CovCor"
      val builder = StringBuilder.newBuilder
      collectionList.addString(builder, ",")
      val msg = "{\"returnTopic\":\"%s\",\"fundNames\":\"%s\"}".format("/getFundNames", builder.toString())

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