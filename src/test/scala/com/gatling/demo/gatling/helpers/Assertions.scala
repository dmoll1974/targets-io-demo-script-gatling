package com.gatling.demo.gatling.helpers


import _root_.io.gatling.core.Predef._
import _root_.io.gatling.core.scenario.Simulation
import _root_.io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.core.result.message.KO
import io.gatling.http.Predef._

class Assertions extends Simulation{



  val httpProtocol = http
    .baseURL(System.getProperty("targetsIoUrl"))
    .extraInfoExtractor(ExtraInfo => {
    if (ExtraInfo.status == KO)
      println("Failed assertion: " + ExtraInfo.request.getUri())
      println("Response: " + ExtraInfo.response.body)
    Nil
  })




  val ltdashHeaders = Map(
    """Content-Type""" -> """application/json""")


  val targetsIoAssertions =
    exec(session => session.set("productName", System.getProperty("productName"))
                           .set("dashboardName", System.getProperty("dashboardName"))
                           .set("testRunId", System.getProperty("testRunId"))
    )
      .exec(http("Get benchmark results for test run")
      .get( """/testrun/${productName}/${dashboardName}/${testRunId}""" )
      .headers(ltdashHeaders)
      .check(jsonPath("$.benchmarkResultPreviousOK").is("true"))
      .check(jsonPath("$.benchmarkResultFixedOK").is("true"))
      .check(jsonPath("$.meetsRequirement").is("true"))
     )

  val assertionsScenario = scenario("assertions")
    .exec(targetsIoAssertions)

  setUp(

    assertionsScenario.inject(atOnceUsers(1))

  ).protocols(httpProtocol)
    .assertions(forAll.failedRequests.count.is(0))


}