package oauth2

import io.getquill._
lazy val ctx = new SqliteJdbcContext(SnakeCase, "ctx")
import ctx._

object Queries:
  def getPlayer(s: String): Option[Int] =
    val q = quote {
      query[PlayerDb].filter(_.name == lift(s)).map(_.id)
    }
    ctx.run(q).headOption

  def getPosition(id: Int): Option[PositionPlayer] =
    val q = quote {
      query[PositionPlayer].filter(_.id == lift(id))
    }
    ctx.run(q).headOption

  def setPosition(id: Int)(x: Double, y: Double, z: Double): Unit =
    val q = quote {
      query[PositionPlayer]
        .insert(
          _.id -> lift(id),
          _.x -> lift(x),
          _.y -> lift(y),
          _.z -> lift(z)
        )
        .onConflictUpdate(_.id)(
          (t, e) => t.x -> e.x,
          (t, e) => t.y -> e.y,
          (t, e) => t.z -> e.z
        )
    }
    ctx.run(q)

  def makePlayer(s: String): Unit =
    val q = quote {
      query[PlayerDb].insert(_.name -> lift(s))
    }
    ctx.run(q)

  def setLogged(id: Int)(logged: Boolean): Unit =
    val q = quote {
      query[LoggedPlayer]
        .insert(_.id -> lift(id), _.logged -> lift(logged))
        .onConflictUpdate(_.id)((t, e) => t.logged -> e.logged)
    }
    ctx.run(q)

  def isLogged(id: Int): Option[Boolean] =
    val q = quote {
      query[LoggedPlayer].filter(_.id == lift(id)).map(_.logged)
    }
    ctx.run(q).headOption

  def getGoogleId(id: Int): Option[String] =
    val q = quote {
      query[GoogleId].filter(_.id == lift(id)).map(_.googleId)
    }
    ctx.run(q).headOption

  def setGoogleId(id: Int)(googleId: String): Unit =
    val q = quote {
      query[GoogleId]
        .insert(_.id -> lift(id), _.googleId -> lift(googleId))
        .onConflictIgnore
    }
    ctx.run(q)
