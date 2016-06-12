package com.gatling.demo.gatling.setup

import com.gatling.demo.gatling.useCases._
import com.gatling.demo.gatling.feeders._
import io.gatling.core.Predef._
import scala.concurrent.duration._

/**
 * This object collects the Scenarios in the project for use in the Simulation. There are two
 * main properties in this object: acceptanceTestScenario and debugScenario. These two are
 * used in the Simulation class to setup the actual tests to run. If you wish to add
 * scenarios to either run, add them here. 
 */
object Scenarios {

  /**
   * These are the scenarios run in 'normal' mode.
   */
  val acceptanceTestScenario = scenario("acceptanceTestScenario")
    .feed(UsersFeeder.users)
    .exec(Home.useCase)
    .repeat(3){
      exec(AddArticle.useCase)
    }
    .repeat(2){
      exec(AddArticle.useCase)
      .exec(OpenArticle.useCase)
    }
  /**
   * These are the scenarios run in 'debug' mode.
   */
  val debugScenario = scenario("debug")
    .feed(UsersFeeder.users)
    .exec(Home.useCase)
    .exec(AddArticle.useCase)
    .exec(OpenArticle.useCase)



}