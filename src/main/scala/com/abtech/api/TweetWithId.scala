package com.abtech.api

import io.circe.generic.auto._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class TweetWithId(id: Int, message: String)

object TweetWithId {
  implicit def tweetWithIdEncoder[F[_]]: EntityEncoder[F, TweetWithId] = jsonEncoderOf[F, TweetWithId]
}
