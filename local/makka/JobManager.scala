package com.oring.smartcity.makka

import java.util.concurrent.TimeUnit

import akka.actor._
import akka.util.Timeout

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by WeiChen on 2016/6/15.
  */


class JobManager extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException ⇒ Resume
      case _: NullPointerException ⇒ Restart
      case _: IllegalArgumentException ⇒ Stop
      case _: Exception ⇒ Escalate
    }

  private def createJobs(jobs: List[String]): Map[String, ActorRef] = {
    jobs.map { j =>
      log.info(j + "is created!")
      val pkgs = j.split("\\.")
      val jobName = pkgs(pkgs.length - 1)
      j -> context.actorOf(
        Props(Class.forName(j).newInstance().asInstanceOf[Actor]),
        name = jobName
      )
    }.toMap
  }

  /*
   * override receive function extends form Actor
   */
  def receive: Receive = {
    case Start() => {
      val eventListener = context.actorOf(Props[EventListenerActor], name = "EventListener")
      //implicit val timeout = Timeout(5 seconds)
      //val future = eventListener ? Start()
      eventListener ! Start()
      //val result = Await.result(future, timeout.duration).asInstanceOf[String]
      println("Return JobManager message!")
      //sender ! result + "\nJobManager Ready!"
    }
    case StartJobs(jobs) => {
      val jobMap = createJobs(jobs)
      jobMap.foreach(j => j._2 ! Start())
    }
    //get response from child actor
    case Pipe((to, data)) => {
      //send(to, data)
    }
    case KillJob(name) => {
      val child = context.actorSelection(name)
      child ! PoisonPill
      log.warning(s"Job $name is killed")
    }
  }

  /*
   * Send data to child actor
   */
  def send(to: String, data: Data): Unit = {
    implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.SECONDS))
    context.actorSelection(to).resolveOne().onComplete {
      case Success(actor) => actor ! data
      case Failure(ex) => log.warning(to + " does not exist!")
    }
  }

}