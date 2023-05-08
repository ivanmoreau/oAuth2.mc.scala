package oauth2

import cats.effect.*, org.http4s.*, org.http4s.dsl.io.*
import cats.effect._
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server._
import cats.syntax.all._
import org.http4s.headers.Location

import cats.effect.unsafe.implicits.global
import org.bukkit.Bukkit
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import org.http4s.ember.client.EmberClientBuilder
import fabric.io.JsonParser

object Service:

  def authUrl(
      redirect_uri: String,
      client_id: String,
      scopes: Seq[String],
      username: String
  ) =
    val scopesStr = scopes.mkString("%20")
    s"https://accounts.google.com/o/oauth2/v2/auth?" ++
      s"client_id=$client_id&" ++
      s"redirect_uri=$redirect_uri&" ++
      s"response_type=code&" ++
      s"scope=$scopesStr" ++
      s"&state=$username"

  val authWithDefaultScopes = authUrl(
    _,
    _,
    Seq(
      "https://www.googleapis.com/auth/userinfo.email",
      "https://www.googleapis.com/auth/userinfo.profile",
      "openid"
    ),
    _
  )

  val googleTokenUrl = "https://oauth2.googleapis.com/token"

  //   --data 'code=AUTHORIZATION_CODE_HERE&client_id=CLIENT_ID_HERE&client_secret=CLIENT_SECRET_HERE&redirect_uri=REDIRECT_URI_HERE&grant_type=authorization_code'

  def getTokenUrl(
      code: String,
      client_id: String,
      client_secret: String,
      redirect_uri: String
  ) =
    val base = uri"https://oauth2.googleapis.com/token"
    val params = Map(
      "client_id" -> Uri.encode(client_id),
      "client_secret" -> Uri.encode(client_secret),
      "redirect_uri" -> Uri.encode(redirect_uri),
      "grant_type" -> Uri.encode("authorization_code")
    )
    base.withQueryParams(params).toString + "&code=" + code.replace("/", "%2F")

  def getToken(code: String): IO[String] =
    val client_id = Config.getClientId
    val client_secret = Config.getClientSecret
    val redirect_uri = s"${Config.getHost}/oauth2/callback"
    val url = getTokenUrl(code, client_id, client_secret, redirect_uri)
    val req = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(url))
    EmberClientBuilder
      .default[IO]
      .build
      .use { client =>
        client.expect[String](req)
      }

  // curl --request GET \
  // --url https://www.googleapis.com/oauth2/v3/userinfo \
  // --header 'Authorization: Bearer ACCESS_TOKEN_HERE'
  def getUserInfoUrl(access_token: String): IO[String] =
    val url = "https://www.googleapis.com/oauth2/v3/userinfo"
    val request = Request[IO](
      method = Method.GET,
      uri = Uri.unsafeFromString(url),
      headers = Headers.of(
        Header("Authorization", s"Bearer $access_token")
      )
    )
    EmberClientBuilder
      .default[IO]
      .build
      .use { client =>
        client.expect[String](request)
      }

  val userService = HttpRoutes
    .of[IO] { case GET -> Root / username =>
      // Bukkit.getPlayer(username).enableActions
      val player = Bukkit.getPlayer(username)
      val url = authWithDefaultScopes(
        s"${Config.getHost}/oauth2/callback",
        Config.getClientId,
        username
      )
      // Ok(s"Hello, $username!, ${value}, ${url}")
      Try(
        Bukkit
          .getLogger()
          .info(
            s"$username is trying to identify itself. Redirecting to Google."
          )
      )
      IO {
        Response[IO]()
          .withStatus(Status.Found)
          .withHeaders(Location(Uri.unsafeFromString(url)))
      }
    }

  object CodeQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")
  object StateQueryParamMatcher
      extends QueryParamDecoderMatcher[String]("state")

  val callback = HttpRoutes
    .of[IO] {
      case GET -> Root / "oauth2" / "callback" :? StateQueryParamMatcher(
            state
          ) +& CodeQueryParamMatcher(
            code
          ) =>
        Bukkit.getLogger().info(s"Got callback from Google for $state")
        Try {
          val token = getToken(code).unsafeRunSync()
          val p = JsonParser(token)("access_token").asString
          val s = getUserInfoUrl(p).unsafeRunSync()
          JsonParser(s)("sub").asString
        } match
          case Failure(exception) =>
            Bukkit
              .getLogger()
              .info(s"Error: ${exception.getMessage()} for $state")
            Ok(s"herror!, ${exception.getMessage()}")
          case Success(sub) =>
            Bukkit.getLogger().info(s"Success: for $state")
            val player = Bukkit.getPlayer(state)
            val id = player.getId.get
            val maybeUser = Queries.getGoogleId(id)
            val res = maybeUser match
              case None =>
                Bukkit
                  .getLogger()
                  .info(s"New user. Setting google id for $state")
                Queries.setGoogleId(id)(sub)
                true
              case Some(value) =>
                sub == value
            if res then
              Bukkit.getLogger().info(s"Enabling actions for $state")
              Try(
                Bukkit
                  .getScheduler()
                  .callSyncMethod(
                    Bukkit.getPluginManager().getPlugin("oauth2"),
                    () => player.enableActions
                  )
              ) match
                case Failure(exception) =>
                  Bukkit
                    .getLogger()
                    .info(s"Error: ${exception.getMessage()} for $state")
                  Ok(
                    s"Hello, $state!, ${exception.getMessage()}. Report this to an admin!"
                  )
                case Success(value) =>
                  Ok(s"Hello, $state!, you are logged in! Return to minecraft!")
            else
              Ok(
                s"YOU ARE NOT $state! (or something went wrong, try again or contact an admin)"
              )
    }

  val routes = (userService <+> callback).adaptErr { err =>
    Bukkit.getLogger().info(s"Error: ${err.getMessage()}")
    err
  }

  def run() =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
      .start
      .unsafeRunSync()
