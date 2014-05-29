package waterfallBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class WaterfallBattleScoreBoardTeams {

	private Scoreboard scoreboard;
	private Objective objective;
	private Team players;
	private Team spectators;

	public WaterfallBattleScoreBoardTeams() {
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		objective = scoreboard.registerNewObjective("Role", "dummy");
		objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

		players = scoreboard.registerNewTeam("Players");
		players.setPrefix(ChatColor.WHITE + "[" + ChatColor.DARK_GREEN
				+ "Player" + ChatColor.WHITE + "] " + ChatColor.RESET);
		spectators = scoreboard.registerNewTeam("Spectators");
		spectators.setPrefix(ChatColor.WHITE + "[" + ChatColor.YELLOW
				+ "Spectator" + ChatColor.WHITE + "] " + ChatColor.RESET);
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

}