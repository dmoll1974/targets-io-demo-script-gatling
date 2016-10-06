package com.gatling.demo.gatling.setup

import com.gatling.demo.gatling.configuration.Configuration
import com.gatling.demo.gatling.helpers.HelperScenarios
import com.gatling.demo.gatling.util.TargetsIoSimulation
import io.gatling.core.Predef._
import io.gatling.core.structure.{PopulationBuilder, ScenarioBuilder}
import scala.concurrent.duration._


/**
 * This Simulation class is responsible for configuring the Scenarios to run, keeping the active
 * profile and configuration in mind. A lot of what happens here depends on the configuration in 
 * the application.conf file. When changes have to be made to which scenarios to run, that
 * information is gathered in the Scenarios object.
 */
class Demo extends TargetsIoSimulation {


  /** 
   * This baseScenario determines whether Debug is active and it should override the default scenarios, or
   * whether it should simply take up the scenarios as defined in Scenarios.acceptanceTestScanario.
   */
  val baseScenario = if (Configuration.isDebugActive) Scenarios.debugScenario else Scenarios.acceptanceTestScenario

  /**
   * The runnableScenarios list will contain injected versions of all of the scenarios that need to be run.
   * This list is constructed by configuring the scenarios in the baseScenario and optionally prepending the
   * list with some auxiliary scenarios when they are needed.
   */
  /**
    * The runnableScenarios list will contain injected versions of all of the scenarios that need to be run.
    * This list is constructed by configuring the scenarios in the baseScenario and optionally prepending the
    * list with some auxiliary scenarios when they are needed.
    */
  val runnableScenarios: List[PopulationBuilder] = addKeepAliveScenario(configureBaseScenarios(List(baseScenario)))


  /**
    * Adds the scenario that sends keepalive requests to the wily-export app.
    * @param scenarios the list of ready-made scenarios
    * @return the list with all the previously prepared scenarios, including the failover scenario if that profile is active.
    */
  def addKeepAliveScenario(scenarios: List[PopulationBuilder]): List[PopulationBuilder] = {
    if (!Configuration.isDebugActive)
      HelperScenarios.runningTestKeepAliveScenario.inject(atOnceUsers(1)) :: scenarios
    else
      scenarios
  }



  /**
   * Recurses over the list of scenarios passed into the function and sets them up with the required users, ramp-up,
   * etc.
   * @param scenarios the list of Scenarios to initialize
   * @return a list of PopulationBuilders, ready to be simulated
   */
  def configureBaseScenarios(scenarios: List[ScenarioBuilder]): List[PopulationBuilder] = scenarios match {
    case Nil => Nil
    case sc :: scs => (if (Configuration.isDebugActive) setupSingleDebugScenario(sc) else setupSingleScenario(sc)) :: configureBaseScenarios(scs)
  }

  /**
   * Injects the required debug settings into a single ScenarioBuilder.
   * @param scn the Scenario to initialize
   * @return the initialized PopulationBuilder
   */
  def setupSingleDebugScenario(scn: ScenarioBuilder): PopulationBuilder = scn.inject(
      atOnceUsers(1)
    ).disablePauses

  /**
   * Injects the required settings into a single ScenarioBuilder.
   * @param scn the Scenario to initialize
   * @return the initialized PopulationBuilder
   */
  def setupSingleScenario(scn: ScenarioBuilder): PopulationBuilder = scn.inject(
      // Each default scenario is delayed by 10 seconds to allow the Mocca caches to be loaded
      rampUsersPerSec(Configuration.initialUsersPerSecond) to Configuration.targetUsersPerSecond during (Configuration.rampUpPeriodInSeconds),
      constantUsersPerSec(Configuration.targetUsersPerSecond) during(Configuration.constantUsagePeriodInSeconds)


    )
    .exponentialPauses


  // Go!
  setUp(runnableScenarios).protocols(if (Configuration.isDebugActive) Configuration.httpDebugProtocol else Configuration.httpProtocol)

}
