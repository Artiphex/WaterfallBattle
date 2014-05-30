package waterfallBattle;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class WaterfallBattleConfig {

	private static final Logger LOGGER = Bukkit.getLogger();

	public static File file = new File("plugins/WaterfallBattle", "config.yml");
	public static FileConfiguration fileConfiguration = YamlConfiguration
			.loadConfiguration(file);

	public static void setDefaults() {
		fileConfiguration.addDefault("language", "de");
		fileConfiguration.addDefault("country", "DE");
		fileConfiguration.addDefault("tickLength", 20L);

		fileConfiguration.addDefault("amountOfPlayersToStart", 2);
		fileConfiguration.addDefault("startDelay", 30);
		fileConfiguration.addDefault("waterOffTimeout", 300);

		fileConfiguration.addDefault("maxNumberOfBlocksPerTick", 2);
		fileConfiguration.addDefault("maxNumberOfItemsPerTick", 3);
		fileConfiguration.addDefault("minBlockExistsTime", 200);
		fileConfiguration.addDefault("maxBlockExistsTime", 300);
	}

	public static void save() {
		try {
			fileConfiguration.save(file);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	public static Locale getLocale() {
		return new Locale(fileConfiguration.getString("language"),
				fileConfiguration.getString("country"));
	}

	public static void setLocale(String language, String country) {
		fileConfiguration.set("language", language);
		fileConfiguration.set("country", country);
		save();
	}

	public static int getStartDelay() {
		return fileConfiguration.getInt("startDelay");
	}

	public static void setStartDelay(int startDelay) {
		fileConfiguration.set("startDelay", startDelay);
		save();
	}

	public static int getWaterOffTimeout() {
		return fileConfiguration.getInt("waterOffTimeout");
	}

	public static void setWaterOffTimeout(int waterOffTimeout) {
		fileConfiguration.set("waterOffTimeout", waterOffTimeout);
		save();
	}

	public static int getAmountOfPlayersToStart() {
		return fileConfiguration.getInt("amountOfPlayersToStart");
	}

	public static void setAmountOfPlayersToStart(int amountOfPlayersToStart) {
		fileConfiguration.set("amountOfPlayersToStart", amountOfPlayersToStart);
		save();
	}

	public static int getMaxNumberOfBlocksPerTick() {
		return fileConfiguration.getInt("maxNumberOfBlocksPerTick");
	}

	public static void setMaxNumberOfBlocksPerTick(int maxNumberOfBlocksPerTick) {
		fileConfiguration.set("maxNumberOfBlocksPerTick",
				maxNumberOfBlocksPerTick);
		save();
	}

	public static int getMaxNumberOfItemsPerTick() {
		return fileConfiguration.getInt("maxNumberOfItemsPerTick");
	}

	public static void setMaxNumberOfItemsPerTick(int maxNumberOfItemsPerTick) {
		fileConfiguration.set("maxNumberOfItemsPerTick",
				maxNumberOfItemsPerTick);
		save();
	}

	public static long getTickLength() {
		return fileConfiguration.getLong("tickLength");
	}

	public static void setTickLength(long tickLength) {
		fileConfiguration.set("tickLength", tickLength);
		save();
	}

	public static int getMinBlockExistsTime() {
		return fileConfiguration.getInt("minBlockExistsTime");
	}

	public static void setMinBlockExistsTime(int minBlockExistsTime) {
		fileConfiguration.set("minBlockExistsTime", minBlockExistsTime);
		save();
	}

	public static int getMaxBlockExistsTime() {
		return fileConfiguration.getInt("maxBlockExistsTime");
	}

	public static void setMaxBlockExistsTime(int maxBlockExistsTime) {
		fileConfiguration.set("maxBlockExistsTime", maxBlockExistsTime);
		save();
	}

}
