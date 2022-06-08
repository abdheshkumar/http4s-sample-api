package com.abtech.api

import cats.effect._
import cats.implicits._
//import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.dsl.Http4sDsl

object Http4ssampleapiRoutes {

  def jokeRoutes[F[_]: Sync](J: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "joke" =>
      for {
        joke <- J.get
        resp <- Ok(joke)
      } yield resp
    }
  }

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "hello" / name =>
      for {
        greeting <- H.hello(HelloWorld.Name(name))
        resp <- Ok(greeting)
      } yield resp
    }
  }

  def tweetRoutes[F[_]: Concurrent](
      tweetService: TweetService[F]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "tweets" / IntVar(tweetId) =>
        tweetService
          .getTweet(tweetId)
          .flatMap(_.fold(NotFound())(Ok(_)))
      case req @ POST -> Root / "tweets" =>
        req.as[Tweet].flatMap(tweetService.addTweet).flatMap(Ok(_))
      case req @ PUT -> Root / "tweets" / IntVar(tweetId) =>
        req
          .as[Tweet]
          .flatMap(tweetService.updateTweet(tweetId, _))
          .flatMap(_.fold(NotFound())(Ok(_)))
      case HEAD -> Root / "tweets" / IntVar(tweetId) =>
        tweetService
          .getTweet(tweetId)
          .flatMap(_.fold(NotFound())(_ => Ok()))
      case DELETE -> Root / "tweets" / IntVar(tweetId) =>
        tweetService
          .deleteTweet(tweetId)
          .flatMap(_ => Ok())
    }
  }
}
