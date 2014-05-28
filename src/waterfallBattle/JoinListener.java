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
		if (player.isOp()) {
			waterfallBattle.score2.addArtiphex(player);
		}
		
		player.setScoreboard(waterfallBattle.score2.getScoreboard());

		waterfallBattle.getPlayers().add(player);
		playerJoinEvent.setJoinMessage("§f[§bWaterfall Battle§f] §f"
				+ player.getName() + " joined the game. §f[§b"
				+ String.valueOf(Bukkit.getOnlinePlayers().length - 1)
				+ "§f|§b" + Bukkit.getMaxPlayers() + "§f]");

		waterfallBattle.resetPlayer(player);

		if (waterfallBattle.getGameStatus() == GameStatus.Lobby
				|| waterfallBattle.getGameStatus() == GameStatus.Startable) {
			player.teleport(waterfallBattle.getLobbyLocation());
		} else {
			waterfallBattle.makeSpectator(player);
		}

		player.getInventory().addItem(waterfallBattle.getInformationBook());
		waterfallBattle.send(
				"Welcome to Waterfall-Battle an ArtiphexLP mini game.", player);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent playerQuitEvent) {
		Player player = playerQuitEvent.getPlayer();

		waterfallBattle.getPlayers().remove(player);

		if (waterfallBattle.getPlaying().contains(player)) {
			waterfallBattle.getPlaying().remove(player);
		}
		playerQuitEvent.setQuitMessage("§f[§bWaterfall Battle§f] "
				+ playerQuitEvent.getPlayer().getName()
				+ " left the game. §f[§b"
				+ String.valueOf(Bukkit.getOnlinePlayers().length - 1)
				+ "§f|§b" + Bukkit.getMaxPlayers() + "§f]");

		if (waterfallBattle.getGameStatus() == GameStatus.Game
				|| waterfallBattle.getGameStatus() == GameStatus.Starting) {
			if (waterfallBattle.getPlaying().size() < 2) {
				if (waterfallBattle.getPlaying().size() > 0) {
					waterfallBattle.send(waterfallBattle.getPlaying().get(0)
							.getPlayer().getName()
							+ " has won this round!");
				}
				waterfallBattle.resetGame();
				waterfallBattle.setupGame();
			}
		} else {
			waterfallBattle.setGameStatus(GameStatus.Lobby);
			waterfallBattle.send("Start canceled");
		}
	}

	public WaterfallBattle getWaterfallBattle() {
		return waterfallBattle;
	}

	public void setWaterfallBattle(WaterfallBattle waterfallBattle) {
		this.waterfallBattle = waterfallBattle;
	}

}
