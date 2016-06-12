package com.gatling.demo.gatling.useCases


import com.gatling.demo.gatling.configuration.Configuration
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import jodd.util.URLDecoder
import scala.concurrent.duration._


object Home{

  val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Cache-Control" -> "no-cache",
    "Pragma" -> "no-cache",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_1 = Map(
    "Accept" -> "application/json, text/plain, */*",
    "Cache-Control" -> "no-cache",
    "Pragma" -> "no-cache")

  val headers_2 = Map(
    "Cache-Control" -> "no-cache",
    "Pragma" -> "no-cache")


  val useCase =
    exec(http("Home")
    .get("/")
    .headers(headers_0))
    .pause(4)
    .exec(http("Home - header ")
      .get("/modules/core/client/views/header.client.view.html")
      .headers(headers_1)
      .resources(http("Home - home")
        .get("/modules/core/client/views/home.client.view.html")
        .headers(headers_2)))
    .pause(7)

    .exec(SignIn.useCase)

}