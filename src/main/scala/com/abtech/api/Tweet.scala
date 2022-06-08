package com.abtech.api
import cats.effect.Concurrent
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._
case class Tweet(message: String)

object Tweet {
  implicit def tweetDecoder[F[_]: Concurrent]: EntityDecoder[F, Tweet] =
    jsonOf[F, Tweet]
}
