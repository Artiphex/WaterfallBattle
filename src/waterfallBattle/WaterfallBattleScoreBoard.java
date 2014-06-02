package waterfallBattle;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class WaterfallBattleScoreBoard {

	private Scoreboard scoreboard;
	private Objective objective;
	private Team team;

	public WaterfallBattleScoreBoard() {
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		objective = scoreboard.registerNewObjective("Height", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName("Höhe");

		team = scoreboard.registerNewTeam("Players");
	}

	public void addPlayer(Player player) {
		team.addPlayer(player);
		update(player);
	}

	public void addPlayers(ArrayList<Player> players) {
		for (Player player : players) {
			team.addPlayer(player);
		}
	}

	public void update(Player player) {
		if (player.getLocation().getY() < 0) {
			scoreboard.resetScores(player.getName());
			team.removePlayer(player);
		} else {
			objective.getScore(player.getName()).setScore(
					(int) player.getLocation().getY());
		}
	}

	public void setScoreboards(ArrayList<Player> players) {
		for (Player player : players) {
			player.getPlayer().setScoreboard(scoreboard);
		}
	}

	public void reset(ArrayList<Player> players) {
		for (Player player : players) {
			scoreboard.resetScores(player.getName());
			team.removePlayer(player.getPlayer());
		}
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

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

}
