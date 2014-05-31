package lang;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import waterfallBattle.WaterfallBattleConfig;

public class Messages {

	private static final String BUNDLE_NAME = "lang.messages";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME, WaterfallBattleConfig.getLocale());

	public static String get(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			e.printStackTrace();
			return "FAILURE";
		}
	}

}
