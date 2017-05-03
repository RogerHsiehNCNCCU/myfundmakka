package com.oring.smartcity.example

import java.io.{File, FileInputStream}

import com.oring.smartcity.makka.Job
import com.oring.smartcity.util.SslUtil
import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import play.api.libs.json.Json

import scala.collection.mutable._

/**
  * Created by PeterHuang on 2016/10/27.
  */

class RemotePubJob extends Job{
  var client: MqttClient = null
  val in = new FileInputStream(new File("src/main/resources/mqttConf.json"))
  val rawJson = Json.parse(in)
  val topics = (rawJson \ "topic").as[String]
  //val mqttBrokerHost = "ssl://"+(rawJson \ "broker").as[String]
  val mqttBrokerHost = "tcp://"+(rawJson \ "broker").as[String]

  println(mqttBrokerHost, topics)

  val persistence = new MemoryPersistence
  val options = new MqttConnectOptions
  //val sslContext = new SslUtil().getSslContext("mytest", getClass.getResourceAsStream("/cacerts"))

  //options.setSocketFactory(sslContext.getSocketFactory)
  //options.setUserName("test")
  //options.setPassword("test".toCharArray())
  client = new MqttClient(mqttBrokerHost, MqttClient.generateClientId, persistence)
  //Connect to MqttBroker
  client.connect(options)

  println(client.isConnected())

  override def init(): Unit = {
    println("Job RemotePub init !")
  }
  override def receiveData(data: String): Unit = {
    val remoteInfoList = data.split(",")
    val jsonStr = "{\"finishRemoteNumber\":\"%s\",\"finishJob\":\"%s\",\"finishCalFundNumber\":\"%s\"}".format(remoteInfoList(0), remoteInfoList(1), remoteInfoList(2))
    mqttPublisher(jsonStr)
  }

  private def mqttPublisher(jsonMsg: String): Unit = {
    val msg = jsonMsg
    val msgTopic = client.getTopic(topics)
    val messagePublish = new MqttMessage(msg.getBytes("utf-8"))

    msgTopic.publish(messagePublish)

    println("Publishing Data, Topic : %s, Message : %s\n".format(msgTopic.getName, msg))
  }
}