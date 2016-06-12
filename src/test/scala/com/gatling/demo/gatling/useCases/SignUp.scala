package com.gatling.demo.gatling.useCases

import com.gatling.demo.gatling.configuration.Configuration
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import jodd.util.URLDecoder
import scala.concurrent.duration._


object SignUp  {

	val headers_2 = Map(
		"Cache-Control" -> "no-cache",
		"Pragma" -> "no-cache")

	val headers_5 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Accept-Encoding" -> "gzip, deflate",
		"Cache-Control" -> "no-cache",
		"Origin" -> "http://172.21.42.150:3001",
		"Pragma" -> "no-cache",
		"Content-Type" -> "application/json;charset=UTF-8")



	val useCase =
	exec(http("SignUp - Authentication html")
		.get("/modules/users/client/views/authentication/authentication.client.view.html")
		.headers(headers_2)
		.resources(http("SignUp - Signup html")
			.get("/modules/users/client/views/authentication/signup.client.view.html")
			.headers(headers_2)))
		.pause(15)
		.exec(http("SignUp - Submit")
			.post("/api/auth/signup")
			.headers(headers_5)
			.body(ElFileBody("useCases/signUp.json")))
		.pause(11)

}