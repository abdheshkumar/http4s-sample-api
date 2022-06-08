package com.abtech.api

import cats.effect.Ref
import cats.effect.kernel.Sync
import cats.effect.std.Random
import cats.syntax.flatMap._
import cats.syntax.functor._

trait TweetService[F[_]] {
  def getTweet(tweetId: Int): F[Option[TweetWithId]]
  def addTweet(tweet: Tweet): F[TweetWithId]
  def updateTweet(id: Int, tweet: Tweet): F[Option[TweetWithId]]
  def deleteTweet(id: Int): F[Unit]
}

class DefaultTweetService[F[_]: Sync](
    database: Ref[F, Map[Int, TweetWithId]],
    rand: Random[F]
) extends TweetService[F] {

  override def getTweet(tweetId: Int): F[Option[TweetWithId]] =
    database.get.map(_.get(tweetId))

  override def addTweet(tweet: Tweet): F[TweetWithId] = {
    for {
      id <- rand.nextInt
      tw = TweetWithId(id, tweet.message)
      _ <- database.modify(m => (m + (tw.id -> tw), tw))
    } yield TweetWithId(id, tweet.message)
  }

  override def updateTweet(id: Int, tweet: Tweet): F[Option[TweetWithId]] = {
    val t = TweetWithId(id, tweet.message)
    database.modify(m => (m.updated(t.id, t), Some(t)))
  }

  override def deleteTweet(id: Int): F[Unit] =
    database.modify(m => (m.removed(id), ()))
}
