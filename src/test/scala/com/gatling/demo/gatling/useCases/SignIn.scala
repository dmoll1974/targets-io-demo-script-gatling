package com.gatling.demo.gatling.useCases

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.session.Expression
import scala.concurrent.duration._


object SignIn
  {

    val headers_0 = Map(
      "If-Modified-Since" -> "Wed, 18 May 2016 11:31:41 GMT",
      "If-None-Match" -> """W/"375-154c3a2b548"""")

    val headers_2 = Map(
      "Accept" -> "application/json, text/plain, */*",
      "Accept-Encoding" -> "gzip, deflate",
      "Content-Type" -> "application/json;charset=UTF-8"


   /*   "Origin" -> "http://172.21.42.150:3001"*/)



    val useCase =
    exec(http("SignIn - Authentication html")
    .get("/modules/users/client/views/authentication/authentication.client.view.html")
    .headers(headers_0)
    .resources(http("SignIn - SignIn html")
      .get("/modules/users/client/views/authentication/signin.client.view.html"))
//    .check(status.is(304)))
    .pause(4)
    .exec(http("SignIn - Submit")
      .post("/api/auth/signin")
      .headers(headers_2)
      .body(ElFileBody("useCases/signIn.json"))
      .check(status.in(200,400))
      .check(regex(""""_id":"(.*)"""").optional.saveAs("user_id"))
    )
    .pause(5)

      .doIf(!_.contains("user_id")) {
        exec(SignUp.useCase)

      }
  }