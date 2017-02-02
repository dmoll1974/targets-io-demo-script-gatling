package com.gatling.demo.gatling.useCases

import io.gatling.core.Predef._
import io.gatling.http.Predef._


object OpenArticle{


  val headers_2 = Map(
    "Cache-Control" -> "no-cache",
    "Pragma" -> "no-cache")

  val headers_1 = Map(
    "Accept" -> "application/json, text/plain, */*",
    "Cache-Control" -> "no-cache",
    "Pragma" -> "no-cache")



  val useCase =

  exec(http("List articles html")
    .get("/modules/articles/client/views/list-articles.client.view.html")
    .headers(headers_2)
    .resources(http("List articles")
      .get("/api/articles")
      .headers(headers_1)
      .check(jsonPath("$[*]._id").findAll.saveAs("articleIds")))
  )
    .exec(http("View article html")
    .get("/modules/articles/client/views/view-article.client.view.html")
    .headers(headers_2)
    .resources(http("View article")
      .get("/api/articles/${articleIds.random()}")
      .headers(headers_1)
      .check(jsonPath("$._id").saveAs("articleId"))
      .check(jsonPath("$.isCurrentUserOwner").saveAs("isCurrentUserOwner"))
    )
    )
    /* Delete is article is by logged in user*/
//    doIf("${isCurrentUserOwner}") {
      exec(http("Delete article")
        .delete("/api/articles/${articleId}?$$state=%7B%22status%22:0%7D")
        .headers(headers_1)
        .resources(http("List articles")
          .get("/api/articles")
          .headers(headers_1)
        )
      )
//    }
}