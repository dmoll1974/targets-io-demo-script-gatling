package com.gatling.demo.gatling.util

import _root_.spray.json.DefaultJsonProtocol
import com.google.gson.Gson
import scala.util.control.Breaks._


import spray.json._
import scalaj.http._


case class Benchmarks(meetsRequirement: Boolean, benchmarkResultFixedOK: Boolean, benchmarkResultPreviousOK: Boolean){}

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val benchmarksFormat = jsonFormat3(Benchmarks)
}

import MyJsonProtocol._

object TargetsIoClient {



  def sendTestRunEvent(host: String, command: String, testRunId: String, buildResultsUrl: String, dashboardName: String, productName: String, productRelease: String) {

    println("sending " + command + " test run call to rest service at host " + host + " with data: testRunId: " + testRunId + ", productName: " + productName + ", productRelease: " + productRelease + ", dashboardName: " + dashboardName + ", buildResultsUrl: " + buildResultsUrl)

    val runningTestUrl = host + "/running-test/" + command

    val runningTest = new targetsIoRunningTest(productName, dashboardName, testRunId, buildResultsUrl, productRelease)

    // convert runningTest to a JSON string
    val runningTestAsJson = new Gson().toJson(runningTest)

    var tries = 0


    breakable {
      for (i <- 0 to 5) {

        try {

          tries += i

          var response = Http(runningTestUrl)
            .postData(runningTestAsJson)
            .header("Content-Type", "application/json")

          if (response.asString.code == 200) {

            println("Call to targets-io succeeded, command sent: " + command + " test run")
            break

          } else {

            println("Something went wrong in the call to targets-io, http status code: " + response.asString.code + ", body: " + response.asString.body)

            if (tries < 5) {
              println("Retrying after 3 seconds...")
              Thread.sleep(3000)
            } else {
              println("Giving up after 5 attempts... please fix manually afterwards in the targets-io dashboard GUI.")
              break
            }


          }


        } catch {
          case e: Exception =>
            println("Exception occured: " + e);
            if (tries < 5) {
              println("Retrying after 3 seconds...")
              Thread.sleep(3000)
            } else {
              println("Giving up after 5 attempts... please fix manually afterwards in the targets-io dashboard GUI.")
              break
            }
        }
      }
    }
  }

  def assertBenchmarkResults(host: String, testRunId: String, dashboardName: String, productName: String): Unit = {

    var tries = 0

    var assertionsOKCount = 0
    val assertTestRunUrl = host + "/benchmarks/" + productName + "/" + dashboardName + "/" + testRunId + "/"

    breakable {
      for (i <- 0 to 5) {
        try {

          tries += i

          println("Sending assertions call to " + assertTestRunUrl)

          var response = Http(assertTestRunUrl).header("Content-Type", "application/json")
          var jsonAST = response.asString.body.parseJson
          var benchmarks = jsonAST.convertTo[Benchmarks]

          if (response.asString.code == 200) {
            println("Assertion call succeeded, checking benchmarks now...")

            if (benchmarks.meetsRequirement equals true) {
              assertionsOKCount = assertionsOKCount + 1
            } else {

              println("******************************************************************************************************")
              println("* Requirements results failed: " + host + "/#!/requirements/" + productName + "/" + dashboardName + "/" + testRunId + "/failed/")
              println("******************************************************************************************************")

            }

            if (benchmarks.benchmarkResultPreviousOK equals true) {

              assertionsOKCount = assertionsOKCount + 1

            } else {

              println("******************************************************************************************************")
              println("* Benchmark to previous build results failed: " + host + "/#!/benchmark-previous-build/" + productName + "/" + dashboardName + "/" + testRunId + "/failed/")
              println("******************************************************************************************************")
            }

            if (benchmarks.benchmarkResultFixedOK equals true) {

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
            } else {

              println("All benchmarks passed!")
              break

            }
          } else {

            if (tries < 5) {
              println("Retrying after 3 seconds...")
              Thread.sleep(3000)
            } else {
              println("Giving up after 5 attempts... please fix manually afterwards in the targets-io dashboard GUI.")
              System.exit(-1)

            }
          }

        } catch {

          case e: Exception =>
            println("Exception occured: " + e)
            if (tries < 5) {
              println("Retrying after 3 seconds...")
              Thread.sleep(3000)
            } else {
              println("Giving up after 5 attempts... please fix manually afterwards in the targets-io dashboard GUI.")
              System.exit(-1)

            }
        }
      }
    }

  }
}
class targetsIoRunningTest( var productName: String, var dashboardName: String, var testRunId: String, var buildResultsUrl: String, var productRelease: String )
