package com.gatling.demo.gatling.util


import _root_.spray.json.DefaultJsonProtocol
import com.google.gson.Gson

import spray.json._
import DefaultJsonProtocol._
import scalaj.http._


case class TestRun(productName: String, productRelease: String, dashboardName: String, testRunId: String, start: String , end: String, baseline: String, previousBuild: String, completed: Boolean, humanReadableDuration: String, meetsRequirement: Boolean, benchmarkResultFixedOK: Boolean, benchmarkResultPreviousOK: Boolean, buildResultsUrl: String, annotations: String, rampUpPeriod: Int, metrics: Int){}

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val testRunFormat = jsonFormat17(TestRun)
}

import MyJsonProtocol._

object TargetsIoClient {



  def sendEvent(host: String, command: String, testRunId: String, buildResultsUrl: String, dashboardName: String, productName: String, productRelease: String) {

    println("sending " + command + " test run call to rest service at host " + host + " with data: testRunId: " + testRunId + ", productName: " + productName + ", productRelease: " + productRelease + ", dashboardName: " + dashboardName + ", buildResultsUrl: " + buildResultsUrl)

    val runningTestUrl = host + "/running-test/" + command

    val runningTest = new targetsIoRunningTest(productName, dashboardName, testRunId, buildResultsUrl, productRelease)

    // convert runningTest to a JSON string
    val runningTestAsJson = new Gson().toJson(runningTest)

    var tries = 0
    val maxTries = 6
    var success = false



    while (tries < maxTries && success == false) {
      try {

        val response = Http(runningTestUrl)
          .postData(runningTestAsJson)
          .header("Content-Type", "application/json")

        println("Response status code: " + response.asString.code)

        if (response.asString.code != 200) {

          println("Something went wrong in the call to targets-io, http status code: " + response.asString.code + ", body: " + response.asString.body)

          if (tries < 5) {
            println("Retrying after 3 seconds...")
            Thread.sleep(3000)
            tries = tries + 1
          } else {
            println("Giving up after 5 attempts... please fix manually afterwards in the targets-io dashboard GUI.")
            success = true

          }
        } else {

          println("Call to targets-io succeeded, " + command + "ing the test!")
          success = true
        }


      } catch {
        case e: Exception =>
          println("Exception occured: " + e);
      }
    }
  }

  def assertBenchmarkResults(host: String, testRunId: String, dashboardName: String, productName: String): Unit = {

    var tries = 0
    val maxTries = 6
    var success = false
    var assertionsOKCount = 0
    val assertTestRunUrl = host + "/testrun/" + productName + "/" + dashboardName + "/" + testRunId + "/"

    while (tries < maxTries && success == false) {
      try {

        println("Sending assertions call to " + assertTestRunUrl )

        val response = Http(assertTestRunUrl).header("Content-Type", "application/json")
        val jsonAST = response.asString.body.parseJson
        val testRun = jsonAST.convertTo[TestRun]

        if (response.asString.code == 200) {
          success = true
          println("Assertion call succeeded, checking benchmarks now!" )


          if (testRun.meetsRequirement equals true || testRun.meetsRequirement equals null) {
            assertionsOKCount = assertionsOKCount + 1
          } else {

            println("******************************************************************************************************")
            println("* Requirements results failed: " + host + "/#!/requirements/" + productName + "/" + dashboardName + "/" + testRunId + "/failed/")
            println("******************************************************************************************************")

          }

          if (testRun.benchmarkResultPreviousOK equals true || testRun.benchmarkResultPreviousOK equals null) {

            assertionsOKCount = assertionsOKCount + 1

          } else {

            println("******************************************************************************************************")
            println("* Benchmark to previous build results failed: " + host + "/#!/benchmark-previous-build/" + productName + "/" + dashboardName + "/" + testRunId + "/failed/")
            println("******************************************************************************************************")
          }

          if (testRun.benchmarkResultFixedOK equals true || testRun.benchmarkResultFixedOK equals null) {

            assertionsOKCount = assertionsOKCount + 1

          } else {

            println("******************************************************************************************************")
            println("* Benchmark to fixed baseline results failed: " + host + "/#!/benchmark-fixed-baseline/" + productName + "/" + dashboardName + "/" + testRunId + "/failed/")
            println("******************************************************************************************************")
          }


          if (assertionsOKCount < 3) {

            println("******************************************************************************************************")
            println("* Job failed due to one or more of the benchmarks failing, please check the logs above for details    *")
            println("******************************************************************************************************")

            System.exit(-1)
          }else{

            println("All benchmarks passed!" )

          }
        }

      } catch {

        case e: Exception =>
          println("Exception occured: " + e);
      }

    }

  }
}
  class targetsIoRunningTest( var productName: String, var dashboardName: String, var testRunId: String, var buildResultsUrl: String, var productRelease: String )



