node {

    def testRunId = env.JOB_NAME + "-" + env.BUILD_NUMBER
    def buildUrl = env.BUILD_URL

    // Get the maven tool.
    // ** NOTE: This 'M3' maven tool must be configured
    // **       in the global configuration.
    def mvnHome = tool 'M3'


//   stage 'Execute load test'

    // Run the test
    sh "${mvnHome}/bin/mvn clean install gatling:execute -Pacc -Pnightly -Passertions -DtestRunId=$testRunId -DbuildResultsUrl=$buildUrl"



}