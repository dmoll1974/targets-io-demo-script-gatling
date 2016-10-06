package com.gatling.demo.gatling.helpers

import com.gatling.demo.gatling.configuration.Configuration

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._


object HelperScenarios {


  val targetsIoHeaders = Map(
    """Content-Type""" -> """application/json""")

  /**
    * Scenario that sends keep alive requests to the targets-io app during the test run.
    */

  val runningTestKeepAliveScenario = scenario("Targets-io Keepalive")
    .exec(session => session
      .set("productName", System.getProperty("productName"))
      .set("dashboardName", System.getProperty("dashboardName"))
      .set("testRunId", System.getProperty("testRunId"))
      .set("buildResultsUrl", System.getProperty("buildResultsUrl"))
      .set("targetsIoUrl", System.getProperty("targetsIoUrl"))
      .set("productRelease", System.getProperty("productRelease"))
      .set("rampUpPeriod", Configuration.rampUpPeriodInSeconds.toSeconds)
    )
    .exec(
      polling
        .every(15 seconds)
        .exec(http("Keep Alive")
          .post("${targetsIoUrl}/running-test/keep-alive")
          .body(StringBody("""{"testRunId":  "${testRunId}","dashboardName":  "${dashboardName}", "productName":  "${productName}", "productRelease":  "${productRelease}", "buildResultsUrl":  "${buildResultsUrl}", "rampUpPeriod":  "${rampUpPeriod}"}""")).asJSON
          .headers(targetsIoHeaders)
          .silent
        )
    )

}
