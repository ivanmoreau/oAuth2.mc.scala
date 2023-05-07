import mill._, scalalib._
import coursier.maven.MavenRepository

object plugin extends ScalaModule {
  override def scalaVersion = "3.2.2"

  def repositoriesTask = T.task { super.repositoriesTask() ++ Seq(
    MavenRepository("https://repo.papermc.io/repository/maven-public/")
  ) }

  def compileIvyDeps = Agg(
    ivy"io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT"
  )
}
