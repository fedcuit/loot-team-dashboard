package com.example.actors

import spray.routing.HttpServiceActor

import scala.xml.Elem

case class Home()

class HomeServiceActor extends HttpServiceActor with HomeService {
  override def receive: Receive = {
    case Home => sender ! Some(homePageContent)
  }
}

trait HomeService {
  val homePageContent: Elem = <html>
    <body>
      <h1>Say hello to
        <i>Team dashboard</i>
        on
        <i>akka and spray</i>
        !</h1>
    </body>
  </html>
}
