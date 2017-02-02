package com.gatling.demo.gatling.feeders


import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

/**
 * Created by x077411 on 12/12/2014.
 */
object UsersFeeder {

  val users = csv("users.csv").random


}
