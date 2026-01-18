package org.emil.hnrpmc.simpleclans.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.emil.hnrpmc.simpleclans.SimpleClans;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static java.util.logging.Level.WARNING;

/**
 * 
 * @author RoinujNosde
 * @since 2.10.2
 */
public class UpdateChecker {

	private final SimpleClans plugin;
	private static final String LATEST_VERSION_URL = "https://api.spiget.org/v2/resources/71242/versions/latest";
	private final String version;
	private final String userAgent;

	public UpdateChecker(SimpleClans plugin) {
		this.plugin = plugin;
		version = plugin.getDescription().getVersion().split("-")[0]; //removing trailing hash
		userAgent = "SimpleClans/" + version;
	}

	/**
	 * Checks if the version installed is up-to-date
	 * 
	 */
    public void check() {
        // Ersetzt BukkitRunnable().runTaskAsynchronously
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(LATEST_VERSION_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("User-Agent", userAgent);

                // Verwende Try-with-Resources für automatisches Schließen des Readers
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonElement parse = JsonParser.parseReader(reader);

                    if (parse.isJsonObject()) {
                        String latestVersion = parse.getAsJsonObject().get("name").getAsString();

                        // plugin.version statt Bukkit-Version
                        if (compareVersions(plugin.getVersion(), latestVersion) < 0) {
                            plugin.getLogger().info("You're running an outdated version ({})." + plugin.getVersion());
                            plugin.getLogger().info("The latest version is {}. Download it at:" + latestVersion);
                            // Link ggf. auf CurseForge oder Modrinth anpassen, da Spigot unüblich für NeoForge ist
                            plugin.getLogger().info("https://www.curseforge.com/minecraft/mc-mods/simpleclans");
                        }
                    }
                }
            } catch (MalformedURLException ignored) {
                // URL falsch formatiert
            } catch (IOException | JsonParseException ex) {
                plugin.getLogger().warn("Error checking the plugin version: {}" , ex.getMessage());
            }
        });
    }

	public static int compareVersions(@NotNull String a, @NotNull String b) {
		String[] aSplit = a.split("\\.");
		String[] bSplit = b.split("\\.");
		int length = Math.max(aSplit.length, bSplit.length);
		for (int i = 0; i < length; i++) {
			int aPart = aSplit.length > i ? Integer.parseInt(aSplit[i]) : 0;
			int bPart = bSplit.length > i ? Integer.parseInt(bSplit[i]) : 0;
			if (aPart != bPart) {
				return Integer.compare(aPart, bPart);
			}
		}
		return 0;
	}

}
