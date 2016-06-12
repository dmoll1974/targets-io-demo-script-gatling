package com.gatling.demo.gatling.helpers


import _root_.io.gatling.core.Predef._
import _root_.io.gatling.core.scenario.Simulation
import _root_.io.gatling.http.Predef._
import io.gatling.core.Predef._
import io.gatling.commons.stats.KO
import io.gatling.http.Predef._

class Assertions extends Simulation{


  var targetsIoUrl = System.getProperty("targetsIoUrl")
  var productName = System.getProperty("productName")
  var dashboardName = System.getProperty("dashboardName")
  var testRunId = System.getProperty("testRunId")

  val httpProtocol = http
    .baseURL(targetsIoUrl)
    .extraInfoExtractor(ExtraInfo => {
    /* if one of the assertion fails, log deeplink url to targets-io dashboard*/
    if (ExtraInfo.status == KO)

      ExtraInfo.requestName match{
        
        case "Get requirements results for test run" =>

          println("Requirements results failed: " + targetsIoUrl + "/#!/requirements/" + productName + "/" + dashboardName + "/" + testRunId + "/failed")

        case "Get benchmark to previous build results" =>

          println("Benchmark to previous build results failed: " + targetsIoUrl + "/#!/benchmark-previous-build/" + productName + "/" + dashboardName + "/" + testRunId + "/failed")

        case "Get benchmark to fixed baseline results" =>

          println("Benchmark to fixed baseline results failed: " + targetsIoUrl + "/#!/benchmark-fixed-baseline/" + productName + "/" + dashboardName + "/" + testRunId + "/failed")

      }

      // println("Response: " + ExtraInfo.response.body)
    Nil
  })




  val ltdashHeaders = Map(
    """Content-Type""" -> """application/json""")


  val targetsIoAssertions =
      exec(session => session.set("productName", productName)
                           .set("dashboardName", dashboardName)
                           .set("testRunId", testRunId))
      .exec(session => {println("Getting benchmark results from: /testrun/" + session("productName").as[String] + "/" + session("dashboardName").as[String] + "/" + session("testRunId").as[String])                   
                      session} )
      .exec(http("Get requirements results for test run")
      .get( """/testrun/${productName}/${dashboardName}/${testRunId}""" )
      .headers(ltdashHeaders)
      .check(jsonPath("$.meetsRequirement").is("true"))
     )
      .exec(http("Get benchmark to previous build results")
      .get( """/testrun/${productName}/${dashboardName}/${testRunId}""" )
      .headers(ltdashHeaders)
      .check(jsonPath("$.benchmarkResultPreviousOK").is("true"))
      )
      .exec(http("Get benchmark to fixed baseline results")
      .get( """/testrun/${productName}/${dashboardName}/${testRunId}""" )
      .headers(ltdashHeaders)
      .check(jsonPath("$.benchmarkResultFixedOK").is("true"))
      )
  val assertionsScenario = scenario("assertions")
    .exec(targetsIoAssertions)

  setUp(

    assertionsScenario.inject(nothingFor(10),atOnceUsers(1)) //allow 10 seconds for the benchmarks to complete

  ).protocols(httpProtocol)
    .assertions(forAll.failedRequests.count.is(0))


}
