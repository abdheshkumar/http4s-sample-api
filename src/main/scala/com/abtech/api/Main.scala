package com.abtech.api

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    Http4ssampleapiServer.stream[IO].compile.drain.as(ExitCode.Success)
}
