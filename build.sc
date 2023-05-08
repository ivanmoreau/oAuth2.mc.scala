import mill._, scalalib._
import coursier.maven.MavenRepository

object plugin extends ScalaModule {
  override def scalaVersion = "3.2.2"

  def repositoriesTask = T.task { super.repositoriesTask() ++ Seq(
    MavenRepository("https://repo.papermc.io/repository/maven-public/")
  ) }

  val http4sVersion = "0.23.18"
  val oauth2Version = "0.17.0-RC1"
  val fabricVersion = "1.10.7"

  def ivyDeps = Agg(
    // "org.xerial" % "sqlite-jdbc" % "3.28.0",
    // "io.getquill" %% "quill-jdbc" % "4.6.0"
    // "org.http4s" %% "http4s-dsl" % http4sVersion,
    // "org.http4s" %% "http4s-ember-server" % http4sVersion,
    // "com.ocadotechnology" %% "sttp-oauth2" % "0.17.0-RC1"
    // "com.ocadotechnology" %% "sttp-oauth2-circe" % "0.17.0-RC1" 
    // "org.typelevel" %%% "fabric-core" % "1.10.7"
    ivy"org.xerial:sqlite-jdbc:3.28.0",
    ivy"io.getquill::quill-jdbc::4.6.0",
    ivy"org.http4s::http4s-dsl::${http4sVersion}",
    ivy"org.http4s::http4s-ember-server::${http4sVersion}",
    ivy"org.http4s::http4s-ember-client::${http4sVersion}",
    ivy"com.ocadotechnology::sttp-oauth2::${oauth2Version}",
    ivy"com.ocadotechnology::sttp-oauth2-circe::${oauth2Version}",
    ivy"org.typelevel::fabric-core::${fabricVersion}",
    ivy"org.typelevel::fabric-io::${fabricVersion}",
  )

  def compileIvyDeps = Agg(
    ivy"io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT"
  )
}
