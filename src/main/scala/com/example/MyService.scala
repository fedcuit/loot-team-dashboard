package com.example

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.example.actors.{Home, HomeServiceActor}
import spray.http.MediaTypes._
import spray.routing._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.xml.Elem

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  private val homeServiceRef: ActorRef = actorRefFactory.actorOf(Props[HomeServiceActor])

  def processFutureResult(future: Future[Any], ctx: RequestContext) {
    import spray.httpx.marshalling.BasicMarshallers.NodeSeqMarshaller

    import scala.concurrent.ExecutionContext.Implicits.global

    future.mapTo[Option[Elem]] onComplete {
      case Success(data) => ctx.complete(data)
      case Failure(data) => ctx.complete("Failure in getting results.")
    }
  }

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { ctx =>
          implicit val timeout = Timeout(5 seconds)
          val future = homeServiceRef ? Home
          processFutureResult(future, ctx)
        }
      }
    } ~
      path("iteration") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              """
                |{
                | "iterationNo": 22,
                | "dayOfIteration": 5,
                | "dayLeftInIteration": 5
                |}
              """.stripMargin
            }
          }
        }
      } ~
      path("small_share") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              """
                |{
                | "name": "Erdong"
                |}
              """.stripMargin
            }
          }
        }
      } ~
      path("team_session") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              """
                |{
                | "name": "Erdong"
                |}
              """.stripMargin
            }
          }
        }
      } ~
      path("honor_roll") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              """
                |{
                | [
                |   {"name": "Erdong", "score": 100},
                |   {"name": "Yan", "score": 100},
                |   {"name": "Yu", "score": 100}
                | ]
                |}
              """.stripMargin
            }
          }
        }
      } ~
      path("the_team") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              """
                |{
                | [
                |   {"name": "Erdong", "role": "Dev", "photo": "https://avatars1.githubusercontent.com/u/167244?v=3&s=460"}
                | ]
                |}
              """.stripMargin
            }
          }
        }
      }
}