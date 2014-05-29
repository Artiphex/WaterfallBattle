package waterfallBattle;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

public class WaterfallBattleListener implements Listener {

	private WaterfallBattle waterfallBattle;

	public WaterfallBattleListener(WaterfallBattle waterfallBattle) {
		this.waterfallBattle = waterfallBattle;
	}

	@EventHandler
	public void on(PlayerInteractEvent playerInteractEvent) {
		if (waterfallBattle.getGameStatus() == GameStatus.Game) {
			if (playerInteractEvent.getItem() != null) {
				if (playerInteractEvent.getItem().getType() == Material.COMPASS) {
					waterfallBattle.getSpectatorMenu().open(
							playerInteractEvent.getPlayer());
				} else if (playerInteractEvent.getItem().getType() == Material.SLIME_BALL) {
					upPlayer(playerInteractEvent, 15);
				} else if (playerInteractEvent.getItem().getType() == Material.MAGMA_CREAM) {
					upPlayer(playerInteractEvent, 30);
				}
			}
		} else {
			playerInteractEvent.setCancelled(true);
			if (playerInteractEvent.getClickedBlock() != null) {
				if (playerInteractEvent.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE
						&& (waterfallBattle.getGameStatus() == GameStatus.Lobby || waterfallBattle
								.getGameStatus() == GameStatus.Startable)) {
					if (waterfallBattle.getPlaying().size() <= 9) {
						waterfallBattle.getMenu().open(
								playerInteractEvent.getPlayer());
					} else {
						waterfallBattle
								.send("There are already 9 players you will be allocated to the observers.",
										playerInteractEvent.getPlayer());
					}
				}
			}
		}
	}

	private void upPlayer(PlayerInteractEvent playerInteractEvent,
			double additionalHeight) {
		double y = playerInteractEvent.getPlayer().getLocation().getY()
				+ additionalHeight;

		if (y > 245) {
			y = 245;
		}

		Location location = playerInteractEvent.getPlayer().getLocation();
		location.setY(y);

		playerInteractEvent.getPlayer().teleport(location);

		if (playerInteractEvent.getPlayer().getInventory().getItemInHand()
				.getAmount() > 1) {
			playerInteractEvent
					.getPlayer()
					.getInventory()
					.getItemInHand()
					.setAmount(
							playerInteractEvent.getPlayer().getInventory()
									.getItemInHand().getAmount() - 1);
		} else {
			playerInteractEvent.getPlayer().getInventory()
					.setItemInHand(new ItemStack(Material.AIR));
		}
	}

	@EventHandler
	public void on(final PlayerRespawnEvent playerRespawnEvent) {
		if (waterfallBattle.getGameStatus() == GameStatus.Starting
				|| waterfallBattle.getGameStatus() == GameStatus.Game) {
			playerRespawnEvent.setRespawnLocation(waterfallBattle
					.getSpectatorStartLocation());
			waterfallBattle.makeSpectator(playerRespawnEvent.getPlayer());
		} else {
			playerRespawnEvent.setRespawnLocation(waterfallBattle
					.getLobbyLocation());
		}
	}

	@EventHandler
	public void on(PlayerDeathEvent playerDeathEvent) {
		Player waterfallBattlePlayer = playerDeathEvent.getEntity().getPlayer();

		if (waterfallBattle.getGameStatus() == GameStatus.Game) {

			waterfallBattle.getPlaying()
					.remove(waterfallBattle.getPlaying().indexOf(
							waterfallBattlePlayer));

			if (waterfallBattle.getPlaying().size() < 2) {
				if (waterfallBattle.getPlaying().size() > 0) {
					waterfallBattle.send(waterfallBattle.getPlaying().get(0)
							.getPlayer().getName()
							+ " has won this round!");
				}
				waterfallBattle.resetGame();
				waterfallBattle.setupGame();
			} else {
				waterfallBattle.makeSpectator(waterfallBattlePlayer);
			}
		}

		if (waterfallBattle.getGameStatus() == GameStatus.Game
				&& !waterfallBattle.getPlaying()
						.contains(waterfallBattlePlayer)) {
			playerDeathEvent.setDeathMessage("");
		} else {
			playerDeathEvent.setDeathMessage("§f[§bWaterfall Battle§f] §f"
					+ waterfallBattlePlayer.getPlayer().getName() + " died.");
		}

	}

	@EventHandler
	public void on(final PlayerPickupItemEvent playerPickupItemEvent) {
		if (!waterfallBattle.getPlaying().contains(
				playerPickupItemEvent.getPlayer())) {
			playerPickupItemEvent.setCancelled(true);
		} else if (playerPickupItemEvent.getItem().getItemStack().getType() == Material.GOLD_HELMET) {
			playerPickupItemEvent.setCancelled(true);
			if (!playerPickupItemEvent
					.getPlayer()
					.getInventory()
					.contains(
							playerPickupItemEvent.getItem().getItemStack()
									.getType())
					&& playerPickupItemEvent.getPlayer().getEquipment()
							.getHelmet() == null) {
				ItemStack goldHelmet = new ItemStack(Material.GOLD_HELMET);
				goldHelmet.addEnchantment(Enchantment.OXYGEN, 3);
				playerPickupItemEvent.getPlayer().getEquipment()
						.setHelmet(goldHelmet);
				Bukkit.getScheduler().scheduleSyncDelayedTask(waterfallBattle,
						new Runnable() {

							@Override
							public void run() {
								playerPickupItemEvent.getPlayer()
										.getEquipment()
										.setHelmet(new ItemStack(Material.AIR));
							}
						}, 600L);
				playerPickupItemEvent.getItem().remove();
			}
		} else if (playerPickupItemEvent.getItem().getItemStack().getType() == Material.SLIME_BALL
				|| playerPickupItemEvent.getItem().getItemStack().getType() == Material.MAGMA_CREAM) {

		} else if (playerPickupItemEvent.getItem().getItemStack().getType() == Material.STICK
				|| playerPickupItemEvent.getItem().getItemStack().getType() == Material.BLAZE_ROD) {

		} else {
			if (playerPickupItemEvent
					.getPlayer()
					.getInventory()
					.contains(
							playerPickupItemEvent.getItem().getItemStack()
									.getType())) {
				playerPickupItemEvent.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void on(EntityDamageEvent entityDamageEvent) {
		if (waterfallBattle.getGameStatus() == GameStatus.Game) {
			if (entityDamageEvent.getCause() == DamageCause.FALL) {
				if (entityDamageEvent instanceof Player) {
					Player player = (Player) entityDamageEvent;
					if (!waterfallBattle.getPlaying().contains(player)) {
						entityDamageEvent.setCancelled(true);
					}
				}
				entityDamageEvent.setCancelled(true);
			}
		} else {
			entityDamageEvent.setCancelled(true);
		}
	}

	@EventHandler
	public void on(EntityDamageByEntityEvent entityDamageByEntityEvent) {
		if (waterfallBattle.getGameStatus() == GameStatus.Game) {
			if (entityDamageByEntityEvent.getDamager() instanceof Player) {
				Player player = (Player) entityDamageByEntityEvent.getDamager();
				if (!waterfallBattle.getPlaying().contains(player)) {
					entityDamageByEntityEvent.setCancelled(true);
				} else if (player.getItemInHand().getType() == Material.BLAZE_ROD
						|| player.getItemInHand().getType() == Material.STICK) {
					if (player.getInventory().getItemInHand().getAmount() > 1) {
						player.getInventory()
								.getItemInHand()
								.setAmount(
										player.getInventory().getItemInHand()
												.getAmount() - 1);
					} else {
						player.getInventory().setItemInHand(
								new ItemStack(Material.AIR));
					}
				}
			}
		}
	}

	@EventHandler
	public void on(PlayerMoveEvent playerMoveEvent) {
		Player player = playerMoveEvent.getPlayer();
		if (waterfallBattle.getPlaying().contains(player)
				&& waterfallBattle.getGameStatus() == GameStatus.Game) {
			waterfallBattle.getScore().update(player);
		}
	}

	@EventHandler
	public void on(PlayerDropItemEvent playerDropItemEvent) {
		if (waterfallBattle.getGameStatus() != GameStatus.Game
				|| !waterfallBattle.getPlaying().contains(
						playerDropItemEvent.getPlayer())) {
			playerDropItemEvent.setCancelled(true);
		}
	}

	@EventHandler
	public void on(WeatherChangeEvent weatherChangeEvent) {
		weatherChangeEvent.setCancelled(true);
	}

	@EventHandler
	public void on(BlockBreakEvent blockBreakEvent) {
		blockBreakEvent.setCancelled(true);
	}

	@EventHandler
	public void on(BlockPlaceEvent blockPlaceEvent) {
		blockPlaceEvent.setCancelled(true);
	}

	public WaterfallBattle getWaterfallBattle() {
		return waterfallBattle;
	}

	public void setWaterfallBattle(WaterfallBattle waterfallBattle) {
		this.waterfallBattle = waterfallBattle;
	}

}
