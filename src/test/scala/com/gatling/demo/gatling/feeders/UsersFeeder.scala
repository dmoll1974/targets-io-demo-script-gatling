package com.gatling.demo.gatling.feeders


import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

/**
 * Created by x077411 on 12/12/2014.
 */
object UsersFeeder {

  val usersSignUp = csv("users-signup.csv").circular
  val usersSignIn = csv("users-signin.csv").circular

}
