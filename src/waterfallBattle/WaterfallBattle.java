package waterfallBattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import messages.Messages;

import org.bukkit.Bukkit;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class WaterfallBattle extends JavaPlugin {

	private static final Logger LOGGER = Bukkit.getLogger();

	private int startDelay;
	private int waterOffTimeout;
	private int amountOfPlayersToStart;
	private int maxNumberOfBlocksPerTick;
	private int maxNumberOfItemsPerTick;
	private int minBlockExistsTime;
	private int maxBlockExistsTime;
	private long tickLength;

	private World world;
	private Location lobbyLocation;
	private Location spectatorStartLocation;
	private Location[] playerStartLocations;
	private GameStatus gameStatus;
	private ArrayList<Player> players;
	private ArrayList<Player> playing;
	private WaterfallBattleScoreBoard score;
	private IconMenu menu;
	private IconMenu spectatorMenu;
	private IconMenu itemMenu;
	private ArrayList<Location> blockLocations;
	private ArrayList<ItemStack> items;
	private ArrayList<Material> materials;
	private double[] waterX;
	private double[] waterZ;
	private int counter = 0;

	@Override
	public void onEnable() {
		LOGGER.warning("The plugin does not support reload.");

		WaterfallBattleConfig.setDefaults();
		tickLength = WaterfallBattleConfig.getTickLength();
		startDelay = WaterfallBattleConfig.getStartDelay();
		waterOffTimeout = WaterfallBattleConfig.getWaterOffTimeout();
		amountOfPlayersToStart = WaterfallBattleConfig.getAmountOfPlayersToStart();
		maxNumberOfBlocksPerTick = WaterfallBattleConfig.getMaxNumberOfBlocksPerTick();
		maxNumberOfItemsPerTick = WaterfallBattleConfig.getMaxNumberOfItemsPerTick();
		minBlockExistsTime = WaterfallBattleConfig.getMinBlockExistsTime();
		maxBlockExistsTime = WaterfallBattleConfig.getMaxBlockExistsTime();

		world = Bukkit.getWorld("world");
		lobbyLocation = new Location(world, 21, 249, 263);
		spectatorStartLocation = new Location(world, 15.5, 260, 76.5);
		spectatorStartLocation.setPitch(90);

		waterX = new double[] { 14, 15, 15.5, 16.5, 15.5 };
		waterZ = new double[] { 76, 77, 75, 76, 76 };

		playerStartLocations = new Location[] { new Location(world, 15.5, 253.5, 79.5), new Location(world, 17.5, 253.5, 78.5),
				new Location(world, 18.5, 253.5, 76.5), new Location(world, 17.5, 253.5, 74.5),
				new Location(world, 15.5, 253.5, 73.5), new Location(world, 13.5, 253.5, 74.5),
				new Location(world, 12.5, 253.5, 76.5), new Location(world, 13.5, 253.5, 78.5) };
		players = new ArrayList<Player>();
		playing = new ArrayList<Player>();

		items = new ArrayList<ItemStack>();
		ItemStack slimeBall = new ItemStack(Material.SLIME_BALL);
		setMeta(slimeBall, "§r§9Ball", "§r§7Shoot Up I");
		items.add(slimeBall);
		ItemStack magmaCream = new ItemStack(Material.MAGMA_CREAM);
		setMeta(magmaCream, "§r§9Ball", "§r§7Shoot Up II");
		items.add(magmaCream);
		ItemStack stick = new ItemStack(Material.STICK);
		setMeta(stick, "§r§9Baton");
		stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
		items.add(stick);
		ItemStack blazeRod = new ItemStack(Material.BLAZE_ROD);
		setMeta(blazeRod, "§r§9Baton");
		blazeRod.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
		items.add(blazeRod);
		ItemStack goldHelmet = new ItemStack(Material.GOLD_HELMET);
		setMeta(goldHelmet, "§r§9Diving Hood");
		goldHelmet.addEnchantment(Enchantment.OXYGEN, 3);
		items.add(goldHelmet);

		materials = new ArrayList<Material>();
		materials.add(Material.STAINED_CLAY);

		getCommand("start").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
				if ((gameStatus == GameStatus.Lobby || gameStatus == GameStatus.Startable) && arg0.isOp()) {
					playing.clear();
					for (int i = 0; i < ((players.size() > 9) ? 9 : players.size()); i++) {
						playing.add(players.get(i));
					}
					startDelay = 10;
					gameStatus = GameStatus.Starting;
					return true;
				} else {
					return false;
				}
			}
		});

		getCommand("water").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
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

		menu = new IconMenu(Messages.get("chooseYourRole"), 9, new IconMenu.OptionClickEventHandler() {

			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				event.setWillClose(true);
				if (event.getName().equals(Messages.get("player"))) {
					if (getPlaying().size() <= 9) {
						if (playing.contains(event.getPlayer())) {
							send(Messages.get("youAreAlreadyAPlayer"), event.getPlayer());
						} else {
							playing.add(event.getPlayer());
							send(Messages.get("youHaveChosenToBeAPlayer"), event.getPlayer());
						}
					} else {
						send(Messages.get("thereAreAlreadyNinePlayers"), event.getPlayer());
					}
				} else {
					if (playing.contains(event.getPlayer())) {
						playing.remove(event.getPlayer());
					}
					send(Messages.get("youHaveChosenToBeASpectator"), event.getPlayer());
				}

			}
		}, this);

		menu.setOption(3, new ItemStack(Material.IRON_SWORD, 1), Messages.get("player"), Messages.get("becomeAPlayer"));
		menu.setOption(5, new ItemStack(Material.ENDER_PEARL, 1), Messages.get("spectator"), Messages.get("becomeASpectator"));

		spectatorMenu = new IconMenu(Messages.get("teleport"), 9, new IconMenu.OptionClickEventHandler() {
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

					Location location = new Location(world, playerLocation.getX() + 5, playerLocation.getY() - 5,
							playerLocation.getZ());
					location.setYaw(90);
					location.setPitch(-20);
					event.getPlayer().teleport(location);
				} else {
					send(event.getName() + " " + Messages.get("isNotplayingAnymore"), event.getPlayer());
				}
			}
		}, this);

		int size = 0;
		int m = 0;

		while (true) {
			if (items.size() <= size) {
				break;
			} else {
				size = size + 9;
			}
		}

		itemMenu = new IconMenu("Item Informationen", size, new IconMenu.OptionClickEventHandler() {

			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				event.setWillClose(true);
			}
		}, this);

		for (ItemStack itemStack : items) {
			itemMenu.setOption(items.indexOf(itemStack), itemStack);
		}

		counter = 0;
		world.setDifficulty(Difficulty.PEACEFUL);
		gameStatus = GameStatus.Lobby;
		waterOn();

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if (gameStatus == GameStatus.Lobby) {
					if (playing.size() >= amountOfPlayersToStart) {
						gameStatus = GameStatus.Startable;
						send(Messages.get("theGameStartsIn") + " " + startDelay);
					}
				} else if (gameStatus == GameStatus.Startable) {
					startDelay--;

					if (playing.size() < amountOfPlayersToStart) {
						gameStatus = GameStatus.Lobby;
						send(Messages.get("startCanceled"));
					}

					if (startDelay <= 10) {
						gameStatus = GameStatus.Starting;
					}
				} else if (gameStatus == GameStatus.Starting) {
					send(Messages.get("theGameStartsIn") + " " + startDelay);
					if (startDelay == 5) {
						setupGame();
					} else if (startDelay < 1) {
						startGame();
					}
					startDelay--;
				} else if (gameStatus == GameStatus.Game) {

					Random random = new Random();
					if (blockLocations.size() < 50) {
						for (int i = 0; i < random.nextInt(maxNumberOfBlocksPerTick); i++) {
							Location location = new Location(world, getRandom(random, 11, 19, waterX), random.nextInt(240), (Math
									.random() * (80 - 72) + 72));

							location.getBlock().setType(materials.get(random.nextInt(materials.size())));
							location.getBlock().setData((byte) 15);

							blockLocations.add(location);
							removeBlockWithDelay(location);
						}
					}

					for (int i = 0; i < random.nextInt(maxNumberOfItemsPerTick); i++) {
						int randomNumber = random.nextInt(waterX.length);
						world.dropItem(new Location(world, waterX[randomNumber], (Math.random() * (240 - 50) + 50),
								waterZ[randomNumber]), items.get(random.nextInt(items.size())));
					}

					if (counter < waterOffTimeout) {
						counter++;
					} else {
						waterOff();
					}
				}
			}
		}, 0L, tickLength);
	}

	@Override
	public void onDisable() {
		for (Location location : blockLocations) {
			location.getBlock().setType(Material.AIR);
		}
	}

	/**
	 * Applys the provided parameters onto the given Itemstack.
	 * 
	 * @param itemStack
	 * @param name
	 * @param lore
	 * @return The modified ItemStack
	 */
	public ItemStack setMeta(ItemStack itemStack, String name, String... lore) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(name);
		itemMeta.setLore(Arrays.asList(lore));
		itemStack.setItemMeta(itemMeta);

		return itemStack;
	}

	/**
	 * Sets the game up
	 */
	@SuppressWarnings("deprecation")
	public void setupGame() {
		for (int i = 0; i < playing.size(); i++) {
			Location location = new Location(world, playerStartLocations[i].getX(), playerStartLocations[i].getY() - 1.5,
					playerStartLocations[i].getZ());
			centerYaw(playerStartLocations[i]);

			world.getBlockAt(location).setType(Material.STAINED_CLAY);
			world.getBlockAt(location).setData((byte) 15);

			playing.get(i).getInventory().clear();

			playing.get(i).setCanPickupItems(true);

			Vector vector = new Vector(0, 0, 0);
			playing.get(i).setVelocity(vector);
			playing.get(i).teleport(playerStartLocations[i]);
		}

		for (Player player : players) {
			if (!getPlaying().contains(player)) {
				makeSpectator(player);
			}
		}
	}

	/**
	 * Starts the game
	 */
	public void startGame() {
		updateSpectatorMenu();

		for (int i = 0; i < playing.size(); i++) {
			Location location = new Location(world, playerStartLocations[i].getX(), playerStartLocations[i].getY() - 1.5,
					playerStartLocations[i].getZ());

			world.getBlockAt(location).setType(Material.AIR);
		}
		score.addPlayers(playing);
		score.setScoreboards(players);
		gameStatus = GameStatus.Game;
		send(Messages.get("theGameStarted"));
	}

	/**
	 * Returns if possible the winner and stops the game.
	 */
	public void stop() {
		if (playing.size() > 0) {
			send(playing.get(0).getName() + " " + Messages.get("hasWon"));
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				for (Player player : players) {
					player.performCommand("hub");
				}
				Bukkit.shutdown();
			}
		}, 200L);
	}

	/**
	 * Resets a player.
	 * 
	 * @param player
	 */
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
		player.getInventory().addItem(getInformationBook());
		ItemStack itemStack = new ItemStack(Material.BEACON);
		setMeta(itemStack, "§9Item Informationen");
		player.getInventory().addItem(itemStack);
	}

	/**
	 * Turns the water on.
	 */
	public void waterOn() {
		world.getBlockAt(new Location(world, 15.5, 250.5, 76.5)).setType(Material.WATER);
	}

	/**
	 * Turns the water off.
	 */
	public void waterOff() {
		world.getBlockAt(new Location(world, 15.5, 250.5, 76.5)).setType(Material.AIR);
	}

	/**
	 * Removes the block at the given location after a certain amount of time.
	 * 
	 * @param location
	 */
	private void removeBlockWithDelay(final Location location) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				location.getBlock().setType(Material.AIR);
				blockLocations.remove(location);
			}
		}, (long) (Math.random() * (maxBlockExistsTime - minBlockExistsTime) + minBlockExistsTime));
	}

	/**
	 * Centers the yaw of the given location.
	 * 
	 * @param location
	 */
	public void centerYaw(Location location) {
		location.setYaw((float) (90 + ((Math.atan2(location.getZ() - 76, location.getX() - 15) * 180 / Math.PI))));
	}

	/**
	 * Sends the given message to all players on the server with the Waterfall
	 * Battle tag as prefix.
	 * 
	 * @param message
	 */
	public void send(String message) {
		Bukkit.broadcastMessage(Messages.get("waterfallBattleTag") + " " + message);
	}

	/**
	 * Sends the given message to the given player on the server with the
	 * Waterfall Battle tag as prefix.
	 * 
	 * @param message
	 */
	public void send(String message, Player player) {
		player.sendMessage(Messages.get("waterfallBattleTag") + " " + message);
	}

	/**
	 * Makes the given player a spectator
	 * 
	 * @param player
	 */
	public void makeSpectator(Player player) {
		for (Player player2 : players) {
			player2.hidePlayer(player);
		}
		player.setAllowFlight(true);
		player.setFlying(true);
		player.teleport(spectatorStartLocation);
		player.setCanPickupItems(false);
		player.getInventory().addItem(new ItemStack(Material.COMPASS));
		updateSpectatorMenu();
	}

	public ItemStack getInformationBook() {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setTitle(Messages.get("gameInformationTitle"));
		bookMeta.setAuthor(Messages.get("gameInformationAuthor"));
		bookMeta.setPages(Arrays.asList((Messages.get("gameInformationPage1")), (Messages.get("gameInformationPage2")),
				(Messages.get("gameInformationPage3")), (Messages.get("gameInformationPage4")),
				(Messages.get("gameInformationPage5"))));
		book.setItemMeta(bookMeta);

		return book;
	}

	private double getRandom(Random random, int start, int end, double... exclude) {
		int randomNumber = start + random.nextInt(end - start + 1 - exclude.length);
		for (double ex : exclude) {
			if (randomNumber < ex) {
				break;
			}
			randomNumber++;
		}
		return randomNumber;
	}

	public void updateSpectatorMenu() {
		spectatorMenu.clear();

		for (int i = 0; i < playing.size(); i++) {
			ItemStack itemStack = new ItemStack(Material.SKULL_ITEM);
			itemStack.setDurability((short) 3);
			spectatorMenu.setOption(i, itemStack, playing.get(i).getName(), Messages.get("teleportTo") + " "
					+ playing.get(i).getName());
		}
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
		return waterOffTimeout;
	}

	public void setCounter(int counter) {
		this.waterOffTimeout = counter;
	}

	public IconMenu getItemMenu() {
		return itemMenu;
	}

	public void setItemMenu(IconMenu itemMenu) {
		this.itemMenu = itemMenu;
	}

	public int getWaterOffTimeout() {
		return waterOffTimeout;
	}

	public void setWaterOffTimeout(int waterOffTimeout) {
		this.waterOffTimeout = waterOffTimeout;
	}

	public int getAmountOfPlayersToStart() {
		return amountOfPlayersToStart;
	}

	public void setAmountOfPlayersToStart(int amountOfPlayersToStart) {
		this.amountOfPlayersToStart = amountOfPlayersToStart;
	}

	public int getMaxNumberOfBlocksPerTick() {
		return maxNumberOfBlocksPerTick;
	}

	public void setMaxNumberOfBlocksPerTick(int maxNumberOfBlocksPerTick) {
		this.maxNumberOfBlocksPerTick = maxNumberOfBlocksPerTick;
	}

	public int getMaxNumberOfItemsPerTick() {
		return maxNumberOfItemsPerTick;
	}

	public void setMaxNumberOfItemsPerTick(int maxNumberOfItemsPerTick) {
		this.maxNumberOfItemsPerTick = maxNumberOfItemsPerTick;
	}

	public long getTickLength() {
		return tickLength;
	}

	public void setTickLength(long tickLength) {
		this.tickLength = tickLength;
	}

}
