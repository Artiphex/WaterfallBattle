package waterfallBattle;

import java.util.Timer;
import java.util.TimerTask;

import lang.Messages;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class WaterfallBattleListener implements Listener {

	private WaterfallBattle waterfallBattle;

	public WaterfallBattleListener(WaterfallBattle waterfallBattle) {
		this.waterfallBattle = waterfallBattle;
	}

	@EventHandler
	public void on(PlayerInteractEvent playerInteractEvent) {
		if (playerInteractEvent.getItem() != null) {
			if (playerInteractEvent.getItem().getType() == Material.BEACON) {
				waterfallBattle.openItemViewInventory(playerInteractEvent.getPlayer());
				playerInteractEvent.setCancelled(true);
			}
		}
		if (waterfallBattle.getGameStatus() == GameStatus.Game) {
			if (playerInteractEvent.getItem() != null) {
				if (playerInteractEvent.getItem().getType() == Material.COMPASS) {
					waterfallBattle.openSpectatorInventory(playerInteractEvent.getPlayer());
				} else if (playerInteractEvent.getItem().getType() == Material.SLIME_BALL) {
					upPlayer(playerInteractEvent, 2.0F);
				} else if (playerInteractEvent.getItem().getType() == Material.MAGMA_CREAM) {
					upPlayer(playerInteractEvent, 3.0F);
				}
			}
		} else {
			playerInteractEvent.setCancelled(true);
			if (playerInteractEvent.getClickedBlock() != null) {
				if (playerInteractEvent.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE
						&& (waterfallBattle.getGameStatus() == GameStatus.Lobby || waterfallBattle.getGameStatus() == GameStatus.Startable)) {
					if (waterfallBattle.getPlaying().size() <= 9) {
						waterfallBattle.openRoleInventory(playerInteractEvent.getPlayer());
					} else {
						waterfallBattle.send(Messages.get("thereAreAlreadyNinePlayers"), playerInteractEvent.getPlayer()); //$NON-NLS-1$
					}
				}
			}
		}
	}

	private void upPlayer(PlayerInteractEvent playerInteractEvent, float n) {
		Vector vector = playerInteractEvent.getPlayer().getVelocity();
		vector.setY(n);
		playerInteractEvent.getPlayer().setVelocity(vector);

		if (playerInteractEvent.getPlayer().getInventory().getItemInHand().getAmount() > 1) {
			playerInteractEvent.getPlayer().getInventory().getItemInHand()
					.setAmount(playerInteractEvent.getPlayer().getInventory().getItemInHand().getAmount() - 1);
		} else {
			playerInteractEvent.getPlayer().getInventory().setItemInHand(new ItemStack(Material.AIR));
		}
	}

	@EventHandler
	public void on(final PlayerRespawnEvent playerRespawnEvent) {
		if (waterfallBattle.getGameStatus() == GameStatus.Starting || waterfallBattle.getGameStatus() == GameStatus.Game) {
			waterfallBattle.makeSpectator(playerRespawnEvent.getPlayer());
			playerRespawnEvent.setRespawnLocation(waterfallBattle.getSpectatorStartLocation());
		} else {
			playerRespawnEvent.setRespawnLocation(waterfallBattle.getLobbyLocation());
		}
	}

	@EventHandler
	public void on(PlayerDeathEvent playerDeathEvent) {
		Player player = playerDeathEvent.getEntity().getPlayer();

		if (waterfallBattle.getGameStatus() == GameStatus.Game) {
			if (waterfallBattle.getPlaying().contains(player)) {
				waterfallBattle.getPlaying().remove(waterfallBattle.getPlaying().indexOf(player));
			}

			if (waterfallBattle.getPlaying().size() < 2) {
				waterfallBattle.stop();
			}
		}

		if (waterfallBattle.getGameStatus() == GameStatus.Game && !waterfallBattle.getPlaying().contains(player)) {
			playerDeathEvent.setDeathMessage(""); //$NON-NLS-1$
		} else {
			playerDeathEvent.setDeathMessage(Messages.get("waterfallBattleTag") + " §f" + player.getPlayer().getName() //$NON-NLS-1$ //$NON-NLS-2$
					+ Messages.get("died")); //$NON-NLS-1$
		}

	}

	@EventHandler
	public void on(final PlayerPickupItemEvent playerPickupItemEvent) {
		if (!waterfallBattle.getPlaying().contains(playerPickupItemEvent.getPlayer())) {
			playerPickupItemEvent.setCancelled(true);
		} else {
			waterfallBattle.send(
					Messages.get("youPickedUp") + " §9"
							+ playerPickupItemEvent.getItem().getItemStack().getItemMeta().getDisplayName() + " §r"
							+ Messages.get("youPickedUp2"), playerPickupItemEvent.getPlayer());

			if (playerPickupItemEvent.getItem().getItemStack().getType() == Material.GOLD_HELMET) {
				playerPickupItemEvent.setCancelled(true);
				if (!playerPickupItemEvent.getPlayer().getInventory()
						.contains(playerPickupItemEvent.getItem().getItemStack().getType())) {
					playerPickupItemEvent.getPlayer().getEquipment().setHelmet(waterfallBattle.getItems().get(4));

					Timer timer = new Timer();
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							playerPickupItemEvent.getPlayer().getEquipment().setHelmet(new ItemStack(Material.AIR));
						}
					}, 45000L);
					playerPickupItemEvent.getItem().remove();
					waterfallBattle.send(Messages.get("itIsAutomaticallyEquipped"), playerPickupItemEvent.getPlayer());
				}
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
						player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount() - 1);
					} else {
						player.getInventory().setItemInHand(new ItemStack(Material.AIR));
					}
				}
			}
		}
	}

	@EventHandler
	public void on(PlayerMoveEvent playerMoveEvent) {
		Player player = playerMoveEvent.getPlayer();
		if (waterfallBattle.getPlaying().contains(player) && waterfallBattle.getGameStatus() == GameStatus.Game) {
			waterfallBattle.getScore().update(player);
			Location location = player.getLocation();
			if (location.getY() > 250) {
				Vector vector = player.getVelocity();
				vector.setY(0.0);
				location.setY(250);
				player.setVelocity(vector);
				player.teleport(location);
			}
		}
	}

	@EventHandler
	public void on(PlayerDropItemEvent playerDropItemEvent) {
		if (waterfallBattle.getGameStatus() != GameStatus.Game
				|| !waterfallBattle.getPlaying().contains(playerDropItemEvent.getPlayer())) {
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

	@EventHandler
	void onInventoryClick(final InventoryClickEvent inventoryClickEvent) {
		final Player player = (Player) inventoryClickEvent.getWhoClicked();

		switch (inventoryClickEvent.getInventory().getTitle()) {
		case "Role":
			inventoryClickEvent.setCancelled(true);
			if (inventoryClickEvent.getCurrentItem().getType() == Material.IRON_SWORD) {
				if (waterfallBattle.getPlaying().size() <= 9) {
					if (waterfallBattle.getPlaying().contains(player)) {
						waterfallBattle.send(Messages.get("youAreAlreadyAPlayer"), player);
					} else {
						waterfallBattle.getPlaying().add(player);
						waterfallBattle.send(Messages.get("youHaveChosenToBeAPlayer"), player);
					}
				} else {
					waterfallBattle.send(Messages.get("thereAreAlreadyNinePlayers"), player);
				}
			} else {
				if (waterfallBattle.getPlaying().contains(player)) {
					waterfallBattle.getPlaying().remove(player);
				}
				waterfallBattle.send(Messages.get("youHaveChosenToBeASpectator"), player);
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(waterfallBattle, new Runnable() {

				@Override
				public void run() {
					inventoryClickEvent.getWhoClicked().closeInventory();
				}
			});
			break;
		case "Teleport":
			inventoryClickEvent.setCancelled(true);
			Player player2 = null;

			for (Player player3 : waterfallBattle.getPlaying()) {
				if (player3.getName().equals(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())) {
					player2 = player3;
					break;
				}
			}

			if (player2 != null) {
				Location playerLocation = player.getLocation();

				Location location = new Location(waterfallBattle.getWorld(), playerLocation.getX() + 5,
						playerLocation.getY() - 5, playerLocation.getZ());
				location.setYaw(90);
				location.setPitch(-20);
				player.teleport(location);
			} else {
				waterfallBattle.send(
						inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName() + " "
								+ Messages.get("isNotplayingAnymore"), player);
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(waterfallBattle, new Runnable() {

				@Override
				public void run() {
					inventoryClickEvent.getWhoClicked().closeInventory();
				}
			});
		case "Items":
			inventoryClickEvent.setCancelled(true);
			break;
		}
	}

	public WaterfallBattle getWaterfallBattle() {
		return waterfallBattle;
	}

	public void setWaterfallBattle(WaterfallBattle waterfallBattle) {
		this.waterfallBattle = waterfallBattle;
	}

}
