package com.gatling.demo.gatling.useCases

import io.gatling.core.Predef._
import io.gatling.http.Predef._


object DeleteArticle{


  val headers_1 = Map(
    "Accept" -> "application/json, text/plain, */*",
    "Cache-Control" -> "no-cache",
    "Pragma" -> "no-cache")



  val useCase =

     exec(http("Delete article")
        .delete("/api/articles/${articleId}?$$state=%7B%22status%22:0%7D")
        .headers(headers_1)
        .check(status.not(403))
        .resources(http("List articles")
          .get("/api/articles")
          .headers(headers_1)
        )
      )

}