package com.oring.smartcity.makka

import akka.actor._
import com.oring.smartcity.util.DummyPublisher
/**
  * Created by WeiChen on 2016/6/15.
  */

class MSController() {
  final def run() {
    val ms = ActorSystem("microservice")
    val jobManager = ms.actorOf(Props[JobManager],name="JobManager")
    //implicit val timeout = Timeout(10 seconds)
    //val future = jobManager ? Start()
    jobManager ! Start()
    //val result = Await.result(future, timeout.duration).asInstanceOf[String]
    println("Return MScontroller message!")
    //result
  }
}
