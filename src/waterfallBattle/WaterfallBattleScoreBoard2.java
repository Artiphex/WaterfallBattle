package waterfallBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class WaterfallBattleScoreBoard2 {

	private Scoreboard scoreboard;
	private Objective objective;
	private Team players;
	private Team spectators;
	private Team undefineds;

	public WaterfallBattleScoreBoard2() {
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		objective = scoreboard.registerNewObjective("Role", "dummy");
		// objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

		players = scoreboard.registerNewTeam("Players");
		// players.setPrefix(ChatColor.AQUA.toString());
		// players.setSuffix(ChatColor.RESET.toString());
		players.setPrefix("[Player]");
		spectators = scoreboard.registerNewTeam("Spectators");
		spectators.setPrefix("[Spectator]");
		// spectators.setPrefix(ChatColor.YELLOW.toString());
		// spectators.setSuffix(ChatColor.RESET.toString());
		undefineds = scoreboard.registerNewTeam("Undefineds");
		// undefineds.setPrefix(ChatColor.GRAY.toString());
		// undefineds.setSuffix(ChatColor.RESET.toString());
	}

	public void addArtiphex(Player player) {
		player.setDisplayName(ChatColor.LIGHT_PURPLE + player.getName()
				+ ChatColor.RESET);
	}

	public void addPlayer(Player player) {
		players.addPlayer(player);
		// player.setScoreboard(scoreboard);
		// player.setDisplayName(ChatColor.AQUA + player.getName()
		// + ChatColor.RESET);
	}

	public void addSpectator(Player player) {
		spectators.addPlayer(player);
		// player.setScoreboard(scoreboard);
		// player.setDisplayName(ChatColor.YELLOW + player.getName()
		// + ChatColor.RESET);
	}

	public void addUndefined(Player player) {
		undefineds.addPlayer(player);
		// player.setScoreboard(scoreboard);
		// player.setDisplayName(ChatColor.GRAY + player.getName()
		// + ChatColor.RESET);
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	public Objective getObjective() {
		return objective;
	}

	public void setObjective(Objective objective) {
		this.objective = objective;
	}

	public Team getPlayers() {
		return players;
	}

	public void setPlayers(Team players) {
		this.players = players;
	}

	public Team getSpectators() {
		return spectators;
	}

	public void setSpectators(Team spectators) {
		this.spectators = spectators;
	}

	public Team getUndefineds() {
		return undefineds;
	}

	public void setUndefineds(Team undefineds) {
		this.undefineds = undefineds;
	}

}