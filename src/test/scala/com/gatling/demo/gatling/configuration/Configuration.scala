package com.gatling.demo.gatling.configuration

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.commons.stats.KO


/**
 * Contains the configuration needed to build the Scenarios to run. All configuration is read from
 * the resources/application.conf file. 
 */
object Configuration {

  // Loads the resources/application.conf file
  val config = ConfigFactory.load()

  val activeEnvironmentId = System.getProperty("activeEnvironment")
  val activeProfileId = System.getProperty("activeProfileId")
  val isDebugActive = activeProfileId == "debug"
  val useProxy = System.getProperty("useProxy") == "true"

  val activeEnvironment = {
    System.out.println("Targeting environment: " + activeEnvironmentId)
    config.getConfig("environment." + activeEnvironmentId)
  }
  val activeProfile = if (config.hasPath("tests.profile." + activeProfileId)) {
    config.getConfig("tests.profile." + activeProfileId)
  } else {
    if (isDebugActive) {
      System.out.println("Activating DEBUG profile")
    } else {
      System.out.println("Requested Test Profile " + activeProfileId + " is not configured explicitly, falling back to default values")
    }
    
    config.getConfig("tests.profile.default")
  }

//  val targetBaseUrlUni = activeEnvironment.getString("targetBaseUrlUni")
  val targetBaseUrl = activeEnvironment.getString("targetBaseUrl")

  // Note, these values are retrieved from the 'default' profile when DEBUG is active, but they are ignored
  val initialUsersPerSecond = activeProfile.getDouble("initialUsersPerSecond")
//  val rampUsers = activeProfile.getInt("rampUsers")
  val targetUsersPerSecond = activeProfile.getDouble("targetUsersPerSecond")
  val rampUpPeriodInSeconds = ( activeProfile.getLong("rampUpPeriodInSeconds"), TimeUnit.SECONDS )
  val timeToFiveMinutesAfterRampUp = ( activeProfile.getLong("rampUpPeriodInSeconds") + 300, TimeUnit.SECONDS )
  val constantUsagePeriodInSeconds = ( activeProfile.getLong("constantUsagePeriodInSeconds"), TimeUnit.SECONDS )

  private val baseHttpProtocol = http
    .baseURL(targetBaseUrl) // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
//    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
    .extraInfoExtractor(ExtraInfo => {
      if(ExtraInfo.status == KO)
        println("httpCode: " + ExtraInfo.response.statusCode + ", body: "+ ExtraInfo.response.body)
      Nil
    })

  private val baseHttpDebugProtocol = http
    .baseURL(targetBaseUrl) // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  def httpDebugProtocol ={
    /* Add proxy if specified */
    if(useProxy) {
      System.out.println("Using proxy at localhost port 8888!")
      baseHttpDebugProtocol.proxy(Proxy("localhost", 8888).httpsPort(8888))
    }else{
      baseHttpDebugProtocol
    }
  }

  def httpProtocol ={
    (baseHttpDebugProtocol)
  }



}
