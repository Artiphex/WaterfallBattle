package waterfallBattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WaterfallBattle extends JavaPlugin {

	private static Logger logger;
	private World world;
	private Location lobbyLocation;
	private Location spectatorStartLocation;
	private Location[] playerStartLocations;
	private GameStatus gameStatus;
	private ArrayList<Player> players;
	private ArrayList<Player> playing;
	private int startableDelay;
	private int startDelay;
	private WaterfallBattleScoreBoard score;
	private IconMenu menu;
	private IconMenu spectatorMenu;
	private ArrayList<Location> blockLocations;

	private ArrayList<ItemStack> items = new ArrayList<ItemStack>();
	private ArrayList<Material> materials = new ArrayList<Material>();
	private double[] waterX = new double[] { 14, 15, 15.5, 16.5, 15.5 };
	private double[] waterZ = new double[] { 76, 77, 75, 76, 76 };

	private int counter;

	@Override
	public void onEnable() {
		logger = getLogger();
		logger.info("[Waterfall Battle] v" + getDescription().getVersion()
				+ " started");

		world = Bukkit.getWorld("world");
		lobbyLocation = new Location(world, 21, 249, 263);
		spectatorStartLocation = new Location(world, 15.5, 260, 76.5);
		spectatorStartLocation.setPitch(90);

		playerStartLocations = new Location[] {
				new Location(world, 15.5, 253.5, 79.5),
				new Location(world, 17.5, 253.5, 78.5),
				new Location(world, 18.5, 253.5, 76.5),
				new Location(world, 17.5, 253.5, 74.5),
				new Location(world, 15.5, 253.5, 73.5),
				new Location(world, 13.5, 253.5, 74.5),
				new Location(world, 12.5, 253.5, 76.5),
				new Location(world, 13.5, 253.5, 78.5) };
		players = new ArrayList<Player>();
		playing = new ArrayList<Player>();

		items.add(new ItemStack(Material.SLIME_BALL));
		items.add(new ItemStack(Material.SLIME_BALL));
		items.add(new ItemStack(Material.MAGMA_CREAM));
		ItemStack stick = new ItemStack(Material.STICK);
		stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		items.add(stick);
		ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
		blazeRod.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
		items.add(blazeRod);
		ItemStack goldHelmet = new ItemStack(Material.GOLD_HELMET);
		goldHelmet.addEnchantment(Enchantment.OXYGEN, 3);
		items.add(goldHelmet);

		materials.add(Material.STAINED_CLAY);

		getCommand("start").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender arg0, Command arg1,
					String arg2, String[] arg3) {
				if ((gameStatus == GameStatus.Lobby || gameStatus == GameStatus.Startable)
						&& arg0.isOp()) {
					playing.clear();
					for (int i = 0; i < ((players.size() > 9) ? 9 : players
							.size()); i++) {
						playing.add(players.get(i));
					}
					gameStatus = GameStatus.Starting;
					return true;
				} else {
					return false;
				}
			}
		});

		getCommand("water").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender arg0, Command arg1,
					String arg2, String[] arg3) {
				if (arg3.length > 0 && arg0.isOp()) {
					if (arg3[0].equalsIgnoreCase("on")) {
						waterOn();
						return true;
					} else if (arg3[0].equalsIgnoreCase("off")) {
						waterOff();
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		});

		setScore(new WaterfallBattleScoreBoard());

		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(new JoinListener(this), this);
		pluginManager.registerEvents(new WaterfallBattleListener(this), this);

		blockLocations = new ArrayList<Location>();

		menu = new IconMenu("Choose your role", 9,
				new IconMenu.OptionClickEventHandler() {
					@Override
					public void onOptionClick(IconMenu.OptionClickEvent event) {
						event.setWillClose(true);
						if (event.getName().equals("Player")) {
							if (getPlaying().size() <= 9) {
								if (playing.contains(event.getPlayer())) {
									send("You are already a player.",
											event.getPlayer());
								} else {
									playing.add(event.getPlayer());
									send("You have chosen to be a "
											+ event.getName(),
											event.getPlayer());
									// waterfallBattleScoreBoardTeams
									// .addPlayer(event.getPlayer());
								}
							} else {
								send("There are already 9 players you will be allocated to the observers.",
										event.getPlayer());
							}
						} else {
							if (playing.contains(event.getPlayer())) {
								playing.remove(event.getPlayer());
							}
							send("You have chosen to be a " + event.getName(),
									event.getPlayer());
							// waterfallBattleScoreBoardTeams.addSpectator(event
							// .getPlayer());
						}

						// event.getPlayer().setScoreboard(score2.getScoreboard());
					}
				}, this);

		menu.setOption(3, new ItemStack(Material.IRON_SWORD, 1), "Player",
				"Become a Player [" + playing.size() + "|9]");
		menu.setOption(5, new ItemStack(Material.ENDER_PEARL, 1), "Spectator",
				"Become a Spectator");

		spectatorMenu = new IconMenu("Teleport to a player", 9,
				new IconMenu.OptionClickEventHandler() {
					@Override
					public void onOptionClick(IconMenu.OptionClickEvent event) {
						event.setWillClose(true);

						Player player = null;

						for (Player player2 : playing) {
							if (player2.getName().equals(event.getName())) {
								player = player2;
								break;
							}
						}

						if (player != null) {
							Location playerLocation = player.getLocation();

							Location location = new Location(world,
									playerLocation.getX() + 5,
									playerLocation.getY() - 5,
									playerLocation.getZ());
							location.setYaw(90);
							location.setPitch(-20);
							event.getPlayer().teleport(location);
						} else {
							send(event.getName() + " is not playing anymore.",
									event.getPlayer());
						}
					}
				}, this);

		setupGame();

		Bukkit.getServer().getScheduler()
				.scheduleSyncRepeatingTask(this, new Runnable() {

					@Override
					public void run() {
						world.setTime(6000L);
						if (gameStatus == GameStatus.Lobby) {
							if (playing.size() > 1) {
								gameStatus = GameStatus.Startable;
								send("The game locks in " + startableDelay);
							}
						} else if (gameStatus == GameStatus.Startable) {
							startableDelay--;
							if (startableDelay < 1) {
								gameStatus = GameStatus.Starting;
								send("The game is locked");
							} else if (startableDelay == 10) {
								send("The game locks in: " + startableDelay);
							}
						} else if (gameStatus == GameStatus.Starting) {
							send("The game starts in " + startDelay);
							if (startDelay == 5) {
								for (int i = 0; i < playing.size(); i++) {
									Location location = new Location(
											world,
											playerStartLocations[i].getX(),
											playerStartLocations[i].getY() - 1.5,
											playerStartLocations[i].getZ());
									centerYaw(playerStartLocations[i]);

									world.getBlockAt(location).setType(
											Material.STAINED_CLAY);
									world.getBlockAt(location).setData(
											(byte) 15);

									playing.get(i).setCanPickupItems(true);
									playing.get(i).teleport(
											playerStartLocations[i]);
								}

								for (Player waterfallBattlePlayer2 : players) {
									if (!getPlaying().contains(
											waterfallBattlePlayer2)) {
										makeSpectator(waterfallBattlePlayer2);
									}
								}
							} else if (startDelay < 1) {
								updateSpectatorMenu();

								for (int i = 0; i < playing.size(); i++) {
									Location location = new Location(
											world,
											playerStartLocations[i].getX(),
											playerStartLocations[i].getY() - 1.5,
											playerStartLocations[i].getZ());

									world.getBlockAt(location).setType(
											Material.AIR);
								}
								score.addPlayers(playing);
								score.setScoreboards(players);
								gameStatus = GameStatus.Game;
								send("The game started.");
							}
							startDelay--;
						} else if (gameStatus == GameStatus.Game) {
							Random random = new Random();
							if (blockLocations.size() < 50) {
								for (int i = 0; i < random.nextInt(2); i++) {
									Location location = new Location(world,
											getRandom(random, 11, 19, waterX),
											random.nextInt(240), (Math.random()
													* (80 - 72) + 72));

									location.getBlock()
											.setType(
													materials.get(random
															.nextInt(materials
																	.size())));
									location.getBlock().setData((byte) 15);

									blockLocations.add(location);
									removeBlock(location);
								}
							}

							for (int i = 0; i < random.nextInt(3); i++) {
								int randomNumber = random
										.nextInt(waterX.length);
								world.dropItem(new Location(world,
										waterX[randomNumber], (Math.random()
												* (240 - 50) + 50),
										waterZ[randomNumber]), items.get(random
										.nextInt(items.size())));
							}

							if (counter < 300) {
								counter++;
							} else {
								waterOff();
							}
						}
					}
				}, 0L, 20L);
	}

	@Override
	public void onDisable() {
		for (Location location : blockLocations) {
			location.getBlock().setType(Material.AIR);
		}
		blockLocations.clear();

		logger.info("[Waterfall Battle] v" + getDescription().getVersion()
				+ " stopped");
	}

	public void lobby(Player player) {
		resetPlayer(player);
		player.teleport(lobbyLocation);
		player.getInventory().addItem(getInformationBook());
	}

	public ItemStack getInformationBook() {
		String about = "Das Spiel wurde von ArtiphexLP entwickelt und programmiert. Wir hoffen, es gefällt euch und wünschen euch ein spannendes Spiel. Bleibt artig!";

		String spiel2 = ChatColor.BOLD
				+ "Ziel:"
				+ ChatColor.RESET.toString()
				+ " Eliminiere die anderen Spieler, indem du sie aus dem Wasserfall schlägst."
				+ "\n\n"
				+ ChatColor.BOLD.toString()
				+ "Klasse:"
				+ ChatColor.RESET.toString()
				+ " Wähle deine Klasse mit Rechtsklick auf den Zaubertisch Die Klassen der Gegner siehst du im TAB-Scoreboard.";
		String spiel1 = ChatColor.BOLD.toString()
				+ "Start:"
				+ ChatColor.RESET.toString()
				+ " Du wirst in Startboxen teleportiert und fällst von Höhe 260."
				+ "\n\n"
				+ ChatColor.BOLD.toString()
				+ "Spezialitems:"
				+ ChatColor.RESET.toString()
				+ " Spawnen zufällig im Wasserfall und du kannst sie für ein offensiveren Spiel benutzen.";
		String spiel3 = ChatColor.BOLD.toString() + "Blöcke:"
				+ ChatColor.RESET.toString()
				+ " Hier kannst du dich für einige Sekunden retten.";

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setTitle(ChatColor.AQUA + "Game Information");
		bookMeta.setAuthor(ChatColor.BLACK + "Artiphex");
		bookMeta.setPages(Arrays.asList(
				(ChatColor.BLUE.toString() + ChatColor.BOLD.toString()
						+ "WaterfallBattle" + ChatColor.RESET.toString() + "\n"
						+ ChatColor.ITALIC + ChatColor.BLACK.toString()
						+ "Ein Minigame von ArtiphexLP"
						+ ChatColor.RESET.toString() + "\n" + "\n"
						+ "1. Das Spiel" + "\n" + "2. Items" + "\n" + "3. Über ArtiphexLP"),
				(ChatColor.BLUE.toString() + ChatColor.BOLD.toString()
						+ "Das Spiel" + ChatColor.RESET.toString()
						+ ChatColor.BLACK.toString() + "\n\n" + spiel2),
				(ChatColor.BLUE.toString() + ChatColor.BOLD.toString()
						+ "Das Spiel" + ChatColor.RESET.toString()
						+ ChatColor.BLACK.toString() + "\n\n" + spiel1),
				(ChatColor.BLUE.toString() + ChatColor.BOLD.toString()
						+ "Das Spiel" + ChatColor.RESET.toString()
						+ ChatColor.BLACK.toString() + "\n\n" + spiel3),
				(ChatColor.BLUE.toString() + ChatColor.BOLD.toString()
						+ "Über ArtiphexLP" + ChatColor.RESET.toString()
						+ ChatColor.BLACK.toString() + "\n\n" + about + "\n\n"
						+ ChatColor.BLUE.toString() + "www.youtube.com/" + "\n" + "artiphexlp")));
		book.setItemMeta(bookMeta);

		return book;
	}

	public void waterOn() {
		world.getBlockAt(new Location(world, 15.5, 250.5, 76.5)).setType(
				Material.WATER);
	}

	public void waterOff() {
		world.getBlockAt(new Location(world, 15.5, 250.5, 76.5)).setType(
				Material.AIR);
	}

	private double getRandom(Random random, int start, int end,
			double... exclude) {
		int randomNumber = start
				+ random.nextInt(end - start + 1 - exclude.length);
		for (double ex : exclude) {
			if (randomNumber < ex) {
				break;
			}
			randomNumber++;
		}
		return randomNumber;
	}

	private void removeBlock(final Location location) {
		Bukkit.getServer().getScheduler()
				.scheduleSyncDelayedTask(this, new Runnable() {

					@Override
					public void run() {
						location.getBlock().setType(Material.AIR);
						blockLocations.remove(location);
					}
				}, (long) (Math.random() * (300 - 200) + 200));
	}

	public void centerYaw(Location location) {
		location.setYaw((float) (90 + ((Math.atan2(location.getZ() - 76,
				location.getX() - 15) * 180 / Math.PI))));
	}

	public void send(String string) {
		Bukkit.broadcastMessage("§f[§bWaterfall Battle§f] §f" + string);
	}

	public void send(String string, Player waterfallBattlePlayer) {
		waterfallBattlePlayer.sendMessage("§f[§bWaterfall Battle§f] §f"
				+ string);
	}

	public boolean checkForGameEnd() {
		if (playing.size() < 2) {
			if (playing.size() > 0) {
				send(playing.get(0).getName() + " has won this round!");
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

				@Override
				public void run() {
					resetGame();
					setupGame();
				}
			}, 500L);
			return true;
		}
		return false;
	}

	public void makeSpectator(Player player) {
		for (Player waterfallBattlePlayer2 : players) {
			waterfallBattlePlayer2.getPlayer().hidePlayer(player.getPlayer());
		}
		player.setAllowFlight(true);
		player.setFlying(true);
		player.teleport(spectatorStartLocation);
		player.setCanPickupItems(false);
		player.getInventory().addItem(new ItemStack(Material.COMPASS));
	}

	public void updateSpectatorMenu() {
		spectatorMenu.clear();

		for (int i = 0; i < playing.size(); i++) {
			ItemStack itemStack = new ItemStack(Material.SKULL_ITEM);
			itemStack.setDurability((short) 3);
			spectatorMenu.setOption(i, itemStack, playing.get(i).getName(),
					"Teleport to " + playing.get(i).getName());
		}
	}

	public void resetPlayer(Player player) {
		for (Player player2 : players) {
			player.showPlayer(player2.getPlayer());
		}
		player.getInventory().clear();
		player.getEquipment().clear();
		player.resetMaxHealth();
		player.setCanPickupItems(false);
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setGameMode(GameMode.SURVIVAL);
	}

	public void resetGame() {
		for (Player waterfallBattlePlayer : players) {
			resetPlayer(waterfallBattlePlayer);
			if (!waterfallBattlePlayer.getPlayer().isDead()) {
				waterfallBattlePlayer.getPlayer().teleport(lobbyLocation);
			}
		}
		for (Location location : blockLocations) {
			location.getBlock().setType(Material.AIR);
		}
		blockLocations.clear();
		score.reset(playing);
	}

	public void setupGame() {
		world.setDifficulty(Difficulty.PEACEFUL);
		playing.clear();
		startableDelay = 20;
		startDelay = 10;
		gameStatus = GameStatus.Lobby;
		counter = 0;
		waterOn();
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public Location getLobbyLocation() {
		return lobbyLocation;
	}

	public void setLobbyLocation(Location lobbyLocation) {
		this.lobbyLocation = lobbyLocation;
	}

	public Location getSpectatorStartLocation() {
		return spectatorStartLocation;
	}

	public void setSpectatorStartLocation(Location spectatorStartLocation) {
		this.spectatorStartLocation = spectatorStartLocation;
	}

	public Location[] getPlayerStartLocations() {
		return playerStartLocations;
	}

	public void setPlayerStartLocations(Location[] playerStartLocations) {
		this.playerStartLocations = playerStartLocations;
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}

	public ArrayList<Player> getPlaying() {
		return playing;
	}

	public void setPlaying(ArrayList<Player> playing) {
		this.playing = playing;
	}

	public int getStartableDelay() {
		return startableDelay;
	}

	public void setStartableDelay(int startableDelay) {
		this.startableDelay = startableDelay;
	}

	public int getStartDelay() {
		return startDelay;
	}

	public void setStartDelay(int startDelay) {
		this.startDelay = startDelay;
	}

	public WaterfallBattleScoreBoard getScore() {
		return score;
	}

	public void setScore(WaterfallBattleScoreBoard score) {
		this.score = score;
	}

	public IconMenu getMenu() {
		return menu;
	}

	public void setMenu(IconMenu menu) {
		this.menu = menu;
	}

	public IconMenu getSpectatorMenu() {
		return spectatorMenu;
	}

	public void setSpectatorMenu(IconMenu spectatorMenu) {
		this.spectatorMenu = spectatorMenu;
	}

	public ArrayList<Location> getBlockLocations() {
		return blockLocations;
	}

	public void setBlockLocations(ArrayList<Location> blockLocations) {
		this.blockLocations = blockLocations;
	}

	public ArrayList<ItemStack> getItems() {
		return items;
	}

	public void setItems(ArrayList<ItemStack> items) {
		this.items = items;
	}

	public ArrayList<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(ArrayList<Material> materials) {
		this.materials = materials;
	}

	public double[] getWaterX() {
		return waterX;
	}

	public void setWaterX(double[] waterX) {
		this.waterX = waterX;
	}

	public double[] getWaterZ() {
		return waterZ;
	}

	public void setWaterZ(double[] waterZ) {
		this.waterZ = waterZ;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

}
