package com.gatling.demo.gatling.util

import com.google.gson.Gson

import scalaj.http._


object TargetsIoClient {

  def sendEvent(host: String, command: String, testRunId: String, buildResultsUrl: String, dashboardName: String, productName: String, productRelease: String ) {

    println( "sending "+ command + " test run call to rest service at host " + host + " with data: testRunId: "+ testRunId + ", productName: " + productName + ", productRelease: " + productRelease + ", dashboardName: "  + dashboardName +  ", buildResultsUrl: " + buildResultsUrl )

    val runningTestUrl = host + "/running-test/" + command

    val runningTest = new targetsIoRunningTest(productName, dashboardName, testRunId, buildResultsUrl, productRelease)

    // convert runningTest to a JSON string
    val runningTestAsJson = new Gson().toJson(runningTest)

    var tries = 0
    var success = false
    val maxTries = 6


    while(tries < maxTries && success == false) {
      try {

        val response = Http(runningTestUrl)
          .postData(runningTestAsJson)
          .header("Content-Type", "application/json")

        if (response.asString.code != 200) {

          println("Something went wrong in the call to targets-io, http status code: " + response.asString.code + ", body: " + response.asString.body)

          if (tries < 5) {
            println("Retrying after 3 seconds...")
            Thread.sleep(3000)
            tries = tries + 1
          } else {
            println("Giving up after 5 attempts... please fix manually afterwards in the targets-io dashboard GUI.")

          }
        }else{

          println("Call to targets-io succeeded, " + command + "ing the test!")
          success = true
        }


      } catch {
        case e: Exception =>
          println("Exception occured: " + e);
      }
    }
  }

}

class targetsIoRunningTest( var productName: String, var dashboardName: String, var testRunId: String, var buildResultsUrl: String, var productRelease: String ) {
}

