package com.happytarn.burstPVP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import com.happytarn.burstPVP.command.BurstPVPCommand;
import com.happytarn.burstPVP.listener.BurstPVPListener;
import com.happytarn.burstPVP.util.ConfigurationManager;
import com.happytarn.burstPVP.util.FileDirectoryStructure;

public class Main extends JavaPlugin {

	public final static Logger log = Logger.getLogger("Minecraft");
	private static ConfigurationManager config;
	private static Main instance;
	private static HashMap<String, Location> locationMap = new HashMap<String, Location>();

	private static ArrayList<Player> winner = new ArrayList<>();
	private static HashMap<String, Player> sankaList = new HashMap<>();
	private static ArrayList<ItemStack> itemList = new ArrayList<>();

	private boolean isGame = false;

	public void onEnable() {
		instance = this;
		// 設定ファイル読込
		loadConfigFile();

		PVPData.init(this);

		PVPData.loadData();

		//イベントリスナー登録
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BurstPVPListener(this),this);
		// コマンド登録
		getServer().getPluginCommand("burstpvp").setExecutor(new BurstPVPCommand(this));
		getServer().getPluginCommand("pvp").setExecutor(new BurstPVPCommand(this));
	}

	public void onDisable() {
		PVPData.saveData();
	}

	/**
	 * 設定ファイルを読み込む
	 */
	public void loadConfigFile() {
		// ファイルマネージャセットアップ
		FileDirectoryStructure.setup();

		// 設定ファイルパスを取得
		String filepath = getDataFolder() + System.getProperty("file.separator") + "config.yml";
		File file = new File(filepath);

		// 設定ファイルが見つからなければデフォルトのファイルをコピー
		if (!file.exists()) {
			this.saveDefaultConfig();
			log.info("config.yml is not found! Created default config.yml!");
		}

		// 設定ファイルを読み込む
		config = new ConfigurationManager();
		try {
			config.load(this);
		} catch (Exception ex) {
			log.warning("an error occured while trying to load the config file.");
			ex.printStackTrace();
		}
	}

	public void sendAllPlayerMessage(String message) {
		getServer().broadcastMessage(message);
	}

	/**
	 * プレイヤーの全回復、および、全エフェクトの除去を行う
	 *
	 * @param player
	 *            対象プレイヤー
	 */
	public void resetPlayerStatus(final Player player) {

		player.setHealth(player.getMaxHealth());
		player.setFallDistance(0);
		player.setFoodLevel(20);
		player.setRemainingAir(player.getMaximumAir());
		Collection<PotionEffect> effects = player.getActivePotionEffects();
		for (PotionEffect e : effects) {
			player.removePotionEffect(e.getType());
		}
	}

	/**
	 * 設定マネージャを返す
	 *
	 * @return ConfigurationManager
	 */
	public static ConfigurationManager getHappyTarnConfig() {
		return config;
	}

	public static Main getInstance() {
		return instance;
	}

	public void burstPVPend() {
		isGame = false;
		// 勝者の移動
		String worldName = config.getString("WINNER_LOCATION").split(",")[0];
		double x = Double.parseDouble(config.getString("WINNER_LOCATION").split(",")[1]);
		double y = Double.parseDouble(config.getString("WINNER_LOCATION").split(",")[2]);
		double z = Double.parseDouble(config.getString("WINNER_LOCATION").split(",")[3]);
		Location winnerLocation = new Location(getServer().getWorld(worldName), x, y, z);
		for (Player winPlayer : sankaList.values()) {
			if (!winPlayer.isOnline()) {
				continue;
			}
			winPlayer.teleport(winnerLocation);
			winner.add(winPlayer);
			ItemStack db = new ItemStack(Material.DIAMOND_BOOTS);
			ItemMeta meta = db.getItemMeta();
			meta.setDisplayName("\247aI can Fly！(ダイア)");
			ArrayList lore = new ArrayList();
			lore.add("\247eFlyingBoots");
			lore.add("\247eSpeed:4");
			meta.setLore(lore);
			db.setItemMeta(meta);
			winPlayer.getInventory().addItem(db);
			sendAllPlayerMessage(new StringBuffer().append(ChatColor.GOLD).append("勝者：").append(ChatColor.GREEN)
					.append(winPlayer.getName()).toString());
		}

		// アイテムの移動
		int chestIndex = 1;
		Chest chest = getChestLocation(chestIndex);
		if (chest == null) {
			return;
		}
		int addItemCount = 0;
		for (ItemStack item : itemList) {
			addItemCount++;
			chest.getInventory().addItem(item);

			// 満タンまで入れた場合
			if (addItemCount == chest.getInventory().getSize()) {
				// 次のチェスト
				chestIndex++;
				chest = getChestLocation(chestIndex);
				if (chest == null) {
					return;
				}
				// アイテムカウントリセット
				addItemCount = 0;
			}
		}
		itemList.clear();
	}

	public void burstPVPstart() {
		isGame = true;
		//前回の勝利者をクリア
		winner = new ArrayList<>();

		//チェストをクリア
		for (int i = 1; i <= 8; i++) {
			if (getChestLocation(i) != null) {
				getChestLocation(i).getInventory().clear();
			}
		}
	}

	public ArrayList<ItemStack> getItemList(){
		return itemList;
	}

	private Chest getChestLocation(int i) {
		String worldName = "world";

		double x = 0;
		double y = 0;
		double z = 0;

		Location chestLocation = null;
		switch (i) {
		case 1:
			worldName = config.getString("LOOT_LOCATION_1").split(",")[0];
			x = Double.parseDouble(config.getString("LOOT_LOCATION_1").split(",")[1]);
			y = Double.parseDouble(config.getString("LOOT_LOCATION_1").split(",")[2]);
			z = Double.parseDouble(config.getString("LOOT_LOCATION_1").split(",")[3]);
			chestLocation = new Location(getServer().getWorld(worldName), x, y, z);
			break;
		case 2:
			worldName = config.getString("LOOT_LOCATION_2").split(",")[0];
			x = Double.parseDouble(config.getString("LOOT_LOCATION_2").split(",")[1]);
			y = Double.parseDouble(config.getString("LOOT_LOCATION_2").split(",")[2]);
			z = Double.parseDouble(config.getString("LOOT_LOCATION_2").split(",")[3]);
			chestLocation = new Location(getServer().getWorld(worldName), x, y, z);
			break;
		case 3:
			worldName = config.getString("LOOT_LOCATION_3").split(",")[0];
			x = Double.parseDouble(config.getString("LOOT_LOCATION_3").split(",")[1]);
			y = Double.parseDouble(config.getString("LOOT_LOCATION_3").split(",")[2]);
			z = Double.parseDouble(config.getString("LOOT_LOCATION_3").split(",")[3]);
			chestLocation = new Location(getServer().getWorld(worldName), x, y, z);
			break;
		case 4:
			worldName = config.getString("LOOT_LOCATION_4").split(",")[0];
			x = Double.parseDouble(config.getString("LOOT_LOCATION_4").split(",")[1]);
			y = Double.parseDouble(config.getString("LOOT_LOCATION_4").split(",")[2]);
			z = Double.parseDouble(config.getString("LOOT_LOCATION_4").split(",")[3]);
			chestLocation = new Location(getServer().getWorld(worldName), x, y, z);
			break;
		case 5:
			worldName = config.getString("LOOT_LOCATION_5").split(",")[0];
			x = Double.parseDouble(config.getString("LOOT_LOCATION_5").split(",")[1]);
			y = Double.parseDouble(config.getString("LOOT_LOCATION_5").split(",")[2]);
			z = Double.parseDouble(config.getString("LOOT_LOCATION_5").split(",")[3]);
			chestLocation = new Location(getServer().getWorld(worldName), x, y, z);
			break;
		case 6:
			worldName = config.getString("LOOT_LOCATION_6").split(",")[0];
			x = Double.parseDouble(config.getString("LOOT_LOCATION_6").split(",")[1]);
			y = Double.parseDouble(config.getString("LOOT_LOCATION_6").split(",")[2]);
			z = Double.parseDouble(config.getString("LOOT_LOCATION_6").split(",")[3]);
			chestLocation = new Location(getServer().getWorld(worldName), x, y, z);
			break;
		case 7:
			worldName = config.getString("LOOT_LOCATION_7").split(",")[0];
			x = Double.parseDouble(config.getString("LOOT_LOCATION_7").split(",")[1]);
			y = Double.parseDouble(config.getString("LOOT_LOCATION_7").split(",")[2]);
			z = Double.parseDouble(config.getString("LOOT_LOCATION_7").split(",")[3]);
			chestLocation = new Location(getServer().getWorld(worldName), x, y, z);
			break;
		case 8:
			worldName = config.getString("LOOT_LOCATION_8").split(",")[0];
			x = Double.parseDouble(config.getString("LOOT_LOCATION_8").split(",")[1]);
			y = Double.parseDouble(config.getString("LOOT_LOCATION_8").split(",")[2]);
			z = Double.parseDouble(config.getString("LOOT_LOCATION_8").split(",")[3]);
			chestLocation = new Location(getServer().getWorld(worldName), x, y, z);
			break;
		default:
			break;
		}

		if (chestLocation.getBlock().getType() == Material.CHEST) {
			// チェストだった場合
			return (Chest) chestLocation.getBlock().getState();
		}

		return null;
	}

	public void movePlayer(Player sender) {

		if (locationMap.isEmpty()) {
			// 設定箇所がなければコマンド呼び出した人のところにテレポ
			Location senderLocation = sender.getLocation();
			for (Player player : getServer().getOnlinePlayers()) {
				if (player.getGameMode() == GameMode.CREATIVE) {
					continue;
				}
				if(player.isDead()){
					continue;
				}
				resetPlayerStatus(player);
				player.teleport(senderLocation);
				player.teleport(senderLocation);
				sankaList.put(player.getName(), player);
			}
		} else {
			// 設定箇所があれば順番にテレポ
			ArrayList<Location> locationList = new ArrayList<>();
			for (Location location : locationMap.values()) {
				locationList.add(location);
			}
			int index = 0;
			for (Player player : getServer().getOnlinePlayers()) {
				if (player.getGameMode() == GameMode.CREATIVE) {
					continue;
				}
				if(player.isDead()){
					continue;
				}
				resetPlayerStatus(player);
				player.teleport(locationList.get(index));
				player.teleport(locationList.get(index));
				sankaList.put(player.getName(), player);
				if (index == (locationList.size() - 1)) {
					index = 0;
				} else {
					index++;
				}
			}
		}
	}

	public HashMap<String, Player> getSankaList() {
		return sankaList;
	}

	public void setLocation(String name, Location location) {
		locationMap.put(name, location);
	}

	public void clearLocation() {
		locationMap.clear();
	}

	public int getLocationMapSize() {
		return locationMap.size();
	}

	public ArrayList<Player> getWinnerList(){
		return winner;
	}

	public void check(Player sender) {
		sender.sendMessage(new StringBuffer().append(ChatColor.GREEN).append("----------").append(ChatColor.RED)
				.append("設定確認").append(ChatColor.GREEN).append("----------").toString());

		sender.sendMessage(new StringBuffer().append(ChatColor.GREEN).append("現在の開始位置個数：").append(ChatColor.WHITE)
				.append(locationMap.size()).toString());
		sender.sendMessage(new StringBuffer().append(ChatColor.GREEN).append("勝者ロケーション：").append(ChatColor.WHITE)
				.append(config.getString("WINNER_LOCATION")).toString());

		for (int i = 1; i <= 8; i++) {
			if (getChestLocation(i) != null) {
				sender.sendMessage(new StringBuffer().append(ChatColor.GREEN).append("報酬チェスト" + i + "：").append(
						ChatColor.WHITE).append("有効").toString());
			} else {
				sender.sendMessage(new StringBuffer().append(ChatColor.GREEN).append("報酬チェスト" + i + "：").append(
						ChatColor.WHITE).append("チェスト以外の座標が設定されています。").toString());
			}
		}
	}

	public boolean isWinnerChestLoc(Location chestloc) {

		boolean isChest = false;

		for (int i = 1; i <= 8; i++) {
			if (getChestLocation(i) != null) {
				Chest chest = getChestLocation(i);
				if(chestloc.getX() == chest.getLocation().getX() &&
						chestloc.getY() == chest.getLocation().getY() &&
						chestloc.getZ() == chest.getLocation().getZ()){
					isChest = true;
				}

			}
		}

		return isChest;
	}

	public boolean isInGame(){
		return isGame;
	}

	public void setGame(boolean game){
		this.isGame = game;
	}


}
