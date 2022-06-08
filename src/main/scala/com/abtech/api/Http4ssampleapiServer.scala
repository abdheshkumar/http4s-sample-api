package com.abtech.api

import cats.effect.std.Random
import cats.effect.{Async, Ref, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object Http4ssampleapiServer {

  def stream[F[_]: Async]: Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)
      ref <- Stream.eval(Ref.of[F, Map[Int, TweetWithId]](Map.empty[Int, TweetWithId]))
      rand <- Stream.eval(Random.scalaUtilRandom[F])
      tweetService = new DefaultTweetService(ref, rand)
      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract segments not checked
      // in the underlying routes.
      httpApp = (
        Http4ssampleapiRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
        Http4ssampleapiRoutes.jokeRoutes[F](jokeAlg) <+>
          Http4ssampleapiRoutes.tweetRoutes(tweetService)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder.default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8081")
          .withHttpApp(finalHttpApp)
          .build >>
        Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
