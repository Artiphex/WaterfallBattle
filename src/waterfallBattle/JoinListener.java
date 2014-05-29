package waterfallBattle;

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
		// --
//		player.setScoreboard(waterfallBattle
//				.getWaterfallBattleScoreBoardTeams().getScoreboard());
		// --

		waterfallBattle.getPlayers().add(player);
		waterfallBattle.resetPlayer(player);

		if (waterfallBattle.getGameStatus() == GameStatus.Lobby
				|| waterfallBattle.getGameStatus() == GameStatus.Startable) {
			waterfallBattle.lobby(player);
		} else {
			waterfallBattle.makeSpectator(player);
		}

		playerJoinEvent.setJoinMessage(Messages.get("waterfallBattleTag") + " "
				+ playerJoinEvent.getPlayer().getName() + " "
				+ Messages.get("joined") + " " + getPlayerCountString());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent playerQuitEvent) {
		Player player = playerQuitEvent.getPlayer();
		waterfallBattle.getPlayers().remove(player);

		if (waterfallBattle.getPlaying().contains(player)) {
			waterfallBattle.getPlaying().remove(player);
		}

		if (waterfallBattle.getGameStatus() == GameStatus.Game
				|| waterfallBattle.getGameStatus() == GameStatus.Starting) {
			if (waterfallBattle.getPlaying().size() < 2) {
				if (waterfallBattle.getPlaying().size() > 0) {
					waterfallBattle.send(waterfallBattle.getPlaying().get(0)
							.getPlayer().getName()
							+ " " + Messages.get("hasWon"));
				}
				waterfallBattle.resetGame();
				waterfallBattle.setupGame();
			}
		}

		playerQuitEvent.setQuitMessage(Messages.get("waterfallBattleTag") + " "
				+ playerQuitEvent.getPlayer().getName() + " "
				+ Messages.get("left") + " " + getPlayerCountString());
	}

	private String getPlayerCountString() {
		return "§f[§b" + String.valueOf(Bukkit.getOnlinePlayers().length)
				+ "§f|§b" + Bukkit.getMaxPlayers() + "§f]";
	}

	public WaterfallBattle getWaterfallBattle() {
		return waterfallBattle;
	}

	public void setWaterfallBattle(WaterfallBattle waterfallBattle) {
		this.waterfallBattle = waterfallBattle;
	}

}
