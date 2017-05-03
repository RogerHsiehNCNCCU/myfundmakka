package com.oring.smartcity.example

import com.oring.smartcity.makka.{Data, Job}
import dao._
import play.api.libs.json.Json

import scala.collection.mutable._

/**
  * Created by PeterHuang on 2016/10/27.
  */

class LoadDataJob4 extends Job{
  private val mfpDAO = new MongoFundPriceDAO()
  private val allDataList = ListBuffer[ArrayBuffer[String]]()

  override def init(): Unit = {
    println("Job LoadData4 init !")
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
    println("LoadDataJob4 GetFundPrices done!")
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

    remoteCalDone(fundNumber)
  }

  private def remoteCalDone(fundNumber: Int): Unit = {
    val remoteInfo = "1,LoadDataJob4," + fundNumber.toString
    val req_data = new Data(remoteInfo)
    pipe("RemotePubJob", req_data)
  }
}