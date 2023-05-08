package oauth2

import org.bukkit.entity.Player

extension (player: Player)
  def isNewPlayer: Boolean =
    true

  def disableActions: Unit =
    player.restoreLocation
    player.setGameMode(org.bukkit.GameMode.SPECTATOR)

  def enableActions: Unit =
    player.setGameMode(org.bukkit.GameMode.SURVIVAL)
    Queries.getPlayer(player.getName()).foreach(Queries.setLogged(_)(true))
    player.restoreLocation

  def logout: Unit =
    Queries.getPlayer(player.getName()).foreach(Queries.setLogged(_)(false))

  def saveLocationToDB: Unit =
    val id = Queries.getPlayer(player.getName())
    lazy val loc = player.getLocation().toVector()
    lazy val (x, y, z) = (loc.getX(), loc.getY(), loc.getZ())
    val logged = id.flatMap(Queries.isLogged).getOrElse(false)
    id match
      case Some(id) if logged => Queries.setPosition(id)(x, y, z)
      case _                  => ()

  def restoreLocation: Unit =
    val id = Queries.getPlayer(player.getName())
    val pos = id.flatMap(Queries.getPosition)
    pos match
      case Some(PositionPlayer(_, x, y, z)) =>
        player.teleport(new org.bukkit.Location(player.getWorld(), x, y, z))
      case None => ()

  def isLogged: Boolean =
    Queries
      .getPlayer(player.getName())
      .flatMap(Queries.isLogged)
      .getOrElse(false)

  def getId: Option[Int] =
    Queries.getPlayer(player.getName())