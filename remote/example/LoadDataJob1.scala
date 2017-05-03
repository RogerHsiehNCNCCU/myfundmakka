package com.oring.smartcity.example

import com.oring.smartcity.makka.{Data,Job}
import play.api.libs.json.Json
import scala.collection.mutable._
import dao._

import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
  * Created by PeterHuang on 2016/10/27.
  */

class LoadDataJob1 extends Job{
  private val mfpDAO = new MongoFundPriceDAO()
  private val allDataList = ListBuffer[ArrayBuffer[String]]()

  override def init(): Unit = {
    println("Job LoadData1 init !")
    getFundPrices(mfpDAO.collectionNameHandler(mfpDAO.mdbe.getCollectionNames))
  }

  override def receiveData(data: String): Unit = {
    println(data)
    val jsonRaw = Json.parse(data)
    val fundNumber = (jsonRaw \ "fundNumber").as[String]
    val calculateType = (jsonRaw \ "calculateType").as[String]
    val fundNameDataString = (jsonRaw \ "fundNameList").as[String]
    val fundNameList = fundNameDataString.split(",")

    calCovCor(fundNumber.toInt, calculateType, fundNameList)
  }

  private def getFundPrices(fundNameList: Array[String]): Unit = {
    println(fundNameList)
    for (i <- 0 until fundNameList.length) {
    //for (i <- 0 until 10) {
      var newDataList = ArrayBuffer[String]()
      newDataList += fundNameList(i)
      println(fundNameList(i))
      val fundData: Array[String] = mfpDAO.readAllByFund(fundNameList(i))
      for (j <- 0 until fundData.length) {
        if (fundData(j) != null) {
          val date = fundData(j).split(", ")(1).split("=")(1)
          val price = fundData(j).split(", ")(2).split("=")(1).split("}")(0)
          newDataList += date
          newDataList += price
        }
      }
      allDataList += newDataList
    }
    println("LoadDataJob1 GetFundPrices done!")
    Thread.sleep(5000)//如果其他job還沒準備好，會只有自己算，所以在這邊暫停一下
    publish()//publish要算哪些Fund到 CovCor Topic ，是local那邊 subscribe的，因為從那邊分配要算的fund
  }

  private def calCovCor(fundNumber: Int, calculateType: String, fundNameList: Array[String]): Unit = {
    var countDays = 0

    calculateType match {
      //case "Historical" => countDays = 5
      case "Historical" => countDays = 1
      case _ => countDays = 1
    }

    println(fundNumber + "---" + calculateType + "---" + fundNameList.length)

    val startCalculate = new CalculateCorCov()
    startCalculate.CalCovariance(fundNumber, countDays, allDataList)
//會等CalCovariance才叫remoteCalDone嗎?
    remoteCalDone(fundNumber)
  }

  private def remoteCalDone(fundNumber: Int): Unit = {
    val remoteInfo = "1,LoadDataJob1," + fundNumber.toString
    val req_data = new Data(remoteInfo) //把string包成Data格式才能Pipe
    pipe("RemotePubJob", req_data)
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