package com.gatling.demo.gatling.useCases

import io.gatling.core.Predef._
import io.gatling.http.Predef._


object AddArticle{


  val headers_1 = Map(
    "Accept" -> "application/json, text/plain, */*",
    "Cache-Control" -> "no-cache",
    "Pragma" -> "no-cache")

  val headers_2 = Map(
    "Cache-Control" -> "no-cache",
    "Pragma" -> "no-cache")

  val headers_5 = Map(
    "Accept" -> "application/json, text/plain, */*",
    "Accept-Encoding" -> "gzip, deflate",
    "Cache-Control" -> "no-cache",
    "Origin" -> "http://172.21.42.150:3001",
    "Pragma" -> "no-cache",
    "Content-Type" -> "application/json;charset=UTF-8"
  )



  val useCase =
    exec(http("Open add article form")
    .get("/modules/articles/client/views/form-article.client.view.html")
    .headers(headers_2))
    .pause(7)
    .exec(http("Post article")
      .post("/api/articles")
      .headers(headers_5)
      .body(RawFileBody("useCases/article.json"))
      .check(jsonPath("$._id").saveAs("articleId"))
    .resources(
      http("View article")
      .get("/api/articles/${articleId}")
      .headers(headers_1),
      http("View article - html")
        .get("/modules/articles/client/views/view-article.client.view.html")
        .headers(headers_2)
    )
  )
}