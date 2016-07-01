package com.gatling.demo.gatling.util

import io.gatling.core.Predef._

/**
  * Requires at least application and version to be exposed as JVM parameters
  */
class TargetsIoSimulation extends Simulation {


  var targetsIoUrl : String = _
  var buildResultsUrl : String = _
  var productRelease : String = _
  if (System.getProperty("buildResultsUrl") != null) buildResultsUrl = System.getProperty("buildResultsUrl") else buildResultsUrl = "MANUAL_TEST"
  if (System.getProperty("targetsIoUrl") != null) targetsIoUrl = System.getProperty("targetsIoUrl") else targetsIoUrl = "http://dashboard.com"
  if (System.getProperty("productRelease") != null) productRelease = System.getProperty("productRelease") else productRelease = ""

  val dashboardName = System.getProperty("dashboardName")
  val productName = System.getProperty("productName")
  val testRunId = System.getProperty("testRunId")

  if (testRunId != "DEBUG") {
    require(targetsIoUrl != null && testRunId != null && productName != null && dashboardName != null)
  }

  def beforeSimulation() {
    if (testRunId != "DEBUG")
      TargetsIoClient.sendEvent(targetsIoUrl, "start", testRunId,  buildResultsUrl, dashboardName, productName, productRelease)

  }

  before {
    beforeSimulation()
  }

  def afterSimulation() {

    if (testRunId != "DEBUG")
      TargetsIoClient.sendEvent(targetsIoUrl, "end", testRunId, buildResultsUrl, dashboardName, productName, productRelease)

  }

  after {
    afterSimulation()
  }
}
