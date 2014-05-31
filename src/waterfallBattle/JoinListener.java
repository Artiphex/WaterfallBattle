package waterfallBattle;

import lang.Messages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

	private WaterfallBattle waterfallBattle;

	public JoinListener(WaterfallBattle waterfallBattle) {
		this.waterfallBattle = waterfallBattle;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent playerJoinEvent) {
		final Player player = playerJoinEvent.getPlayer();

		waterfallBattle.getPlayers().add(player);
		waterfallBattle.resetPlayer(player);

		if (waterfallBattle.getGameStatus() == GameStatus.Lobby || waterfallBattle.getGameStatus() == GameStatus.Startable) {
			player.teleport(waterfallBattle.getLobbyLocation());
		} else {
			waterfallBattle.makeSpectator(player);
		}

		playerJoinEvent.setJoinMessage(Messages.get("waterfallBattleTag") + " " + playerJoinEvent.getPlayer().getName() + " "
				+ Messages.get("joined") + " " + getPlayerCountString());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent playerQuitEvent) {
		Player player = playerQuitEvent.getPlayer();
		waterfallBattle.getPlayers().remove(player);

		if (waterfallBattle.getPlaying().contains(player)) {
			waterfallBattle.getPlaying().remove(player);
		}

		if (waterfallBattle.getGameStatus() == GameStatus.Game || waterfallBattle.getGameStatus() == GameStatus.Starting) {
			if (waterfallBattle.getPlaying().size() < waterfallBattle.getAmountOfPlayersToStart()) {
				waterfallBattle.stop();
			}
		}

		playerQuitEvent.setQuitMessage(Messages.get("waterfallBattleTag") + " " + playerQuitEvent.getPlayer().getName() + " "
				+ Messages.get("left") + " " + getPlayerCountString());
	}

	private String getPlayerCountString() {
		return "§f[§9" + String.valueOf(Bukkit.getOnlinePlayers().length) + "§f|§9" + Bukkit.getMaxPlayers() + "§f]";
	}

	public WaterfallBattle getWaterfallBattle() {
		return waterfallBattle;
	}

	public void setWaterfallBattle(WaterfallBattle waterfallBattle) {
		this.waterfallBattle = waterfallBattle;
	}

}
