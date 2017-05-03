package com.oring.smartcity.example

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.{Calendar, Date}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.math._
import dao._

import scala.collection.mutable
/**
  * Created by PeterHuang on 2016/10/27.
  */

class CalculateCorCov {
  private val dataStoreToDB = new MongoFundCovarianceDAO2
  //var finalfinalList : Array[Array[String]]
  //var finalfinalList = Array.ofDim[String](80000,998)
  //var finalfinalList = ArrayBuffer[Array[String]]
  //var countfinal = 0

  def CalCovariance(fundNumberForCal: Int, countDays: Int, data: ListBuffer[ArrayBuffer[String]]) = {
    var startPositionA = 0
    var startPositionB = 0

    for(j <- fundNumberForCal + 1 until data.length){//從要算的那支基金往後，因為只算三角形 AB算過 BA不算了
      var foundPosition: Boolean = false
      var countEnd: Boolean = false
      var count = 0
      val dateList = ListBuffer[List[Int]]()

      //找到兩支最後日期一樣的date
      for(m <- data(fundNumberForCal).length - 2 until 0 by -2 if !countEnd) {
        foundPosition = false
        for (n <- data(j).length - 2 until 0 by -2 if !foundPosition) {
          if (data(fundNumberForCal)(m) == data(j)(n)) {
            count = count + 1
            startPositionA = m
            startPositionB = n
            dateList += List(startPositionA, startPositionB)
            foundPosition = true
          }
        }
        if (count == countDays)
          countEnd = true
      }
      println("Start Calculate (" + fundNumberForCal + "," + j + ")")
      Calculate(data(fundNumberForCal), data(j), "1y", dateList)
      Calculate(data(fundNumberForCal), data(j), "3y", dateList)
      println("Finish Calculate (" + fundNumberForCal + "," + j + ")")
    }
    dataStoreToDB.saveCovariance3()
    println("Done!")
  }

  private def Calculate(DataF: ArrayBuffer[String], DataS: ArrayBuffer[String], Interval: String, StartPositionList: ListBuffer[List[Int]]): Unit = {
    var timeLength = 0
    var finalCovCorList = ListBuffer[String]()

    finalCovCorList += DataF(0)
    finalCovCorList += DataS(0)
    finalCovCorList += Interval

    Interval match {
      case "1y" =>  timeLength = 1
      case "3y" =>  timeLength = 3
    }

    val dateFormat = new SimpleDateFormat("yyyy/MM/dd")
    val dateS:Calendar = Calendar.getInstance()
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    for(i <- 0 until StartPositionList.length){
      var sumA = 0.0
      var sumB = 0.0
      var count = 0.0
      var averageA = 0.0
      var averageB = 0.0
      var cov = 0.0
      var productAB = 0.0
      var aMinusABar = 0.0
      var bMinusBBar = 0.0
      var sdA = 0.0
      var sdB = 0.0
      var correlationAB = 0.0

      var valueOfThatDateF = StartPositionList(i)(0) + 1
      var valueOfThatDateS = StartPositionList(i)(1) + 1
      val dt1 = DataF(valueOfThatDateF - 1)
      val dt1parse = LocalDate.parse(dt1, formatter)
      dateS.setTime(dateFormat.parse(dt1))
      dateS.add(Calendar.YEAR,-timeLength) //minus interval
      val dt2:Date = dateS.getTime()
      val dt2str:String = dateFormat.format(dt2)
      val dt2parse = LocalDate.parse(dt2str, formatter)

      for(i <- 0 until (dt1parse.toEpochDay - dt2parse.toEpochDay).toInt){
        if(valueOfThatDateF > 0 && valueOfThatDateS > 0){
          sumA = sumA + DataF(valueOfThatDateF).toFloat
          sumB = sumB + DataS(valueOfThatDateS).toFloat
          count = count + 1
          valueOfThatDateF -= 2
          valueOfThatDateS -= 2
        }
      }
      averageA = sumA / count
      averageB = sumB / count
      var valueOfThatDateF2 = StartPositionList(i)(0) + 1
      var valueOfThatDateS2 = StartPositionList(i)(1) + 1
      for(i <- 0 until (dt1parse.toEpochDay - dt2parse.toEpochDay).toInt){
        if(valueOfThatDateF2 > 0 && valueOfThatDateS2 > 0){
          aMinusABar = DataF(valueOfThatDateF2).toFloat - averageA
          bMinusBBar = DataS(valueOfThatDateS2).toFloat - averageB
          productAB = aMinusABar * bMinusBBar
          sdA = sdA + pow(aMinusABar, 2.0)
          sdB = sdB + pow(bMinusBBar, 2.0)
          cov = cov + productAB
          valueOfThatDateF2 -= 2
          valueOfThatDateS2 -= 2
        }
      }
      cov = cov / (count - 1)
      correlationAB = cov / pow(sdA * sdB, 0.5)
      finalCovCorList += DataF(StartPositionList(i)(0))
      finalCovCorList += cov.toString
      finalCovCorList += "%2.14f".format(correlationAB)
      //println(finalCovCorList)
    }
    println(finalCovCorList)
    val finalList: Array[String] = finalCovCorList.toArray
    //finalfinalList(countfinal) = finalList
    //countfinal = countfinal +1
    //println(countfinal+"!!!!!!!!!")
      //暫時先測試用
      //val fffList:Array[Array[String]] = finalfinalList
    dataStoreToDB.saveCovariance2(finalList)
  }
}
