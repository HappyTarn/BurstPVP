package com.happytarn.burstPVP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.happytarn.burstPVP.util.TextFileHandler;

public class PVPData {

	private static final String PVPDataPath = "plugins/BurstPVP/PVPData.ncsv";

	// 勝敗結果
	private static Map<String, String[]> PVPResultMap = new HashMap<String, String[]>();

	// ＰＶＰリクエスト
	private static HashMap<String, String> pvpRequestFromTo = new HashMap<>();
	private static HashMap<String, String> pvpRequestToFrom = new HashMap<>();

	private static HashMap<String, Inventory> winnerMap = new HashMap<>();

	private static Main plugin = null;

	private static Player player1;
	private static Player player2;
	private static Location oldLocation1;
	private static Location oldLocation2;

	public static Main getPlugin() {
		return plugin;
	}

	public static void setPlugin(Main plugin) {
		PVPData.plugin = plugin;
	}

	public PVPData() {
	}

	public static void init(Main main) {
		setPlugin(main);
	}

	public static boolean loadData() {
		TextFileHandler datafile = new TextFileHandler(PVPDataPath);
		PVPResultMap.clear(); // ハッシュマップをクリア
		try {
			List<String> list = datafile.readLines();
			String[] data;

			@SuppressWarnings("unused")
			int line = 0;
			// 行を格納したリストが空になるまで繰り返す
			while (!list.isEmpty()) {
				line++; // ログ用のデータファイル行数
				String thisLine = list.remove(0); // リストの先頭にある要素を格納して削除

				// デリミタで配列に分ける
				data = thisLine.split(",");

				// 行の形式がおかしい場合はその行をスキップ
				if (data.length != 4) {
					continue;
				}

				// ハッシュマップにコーティング黒曜石データを設置
				PVPResultMap.put(data[0], data);
			}
		} catch (FileNotFoundException ex) {
			return false;
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	public static boolean saveData() {
		TextFileHandler datafile = new TextFileHandler(PVPDataPath);
		// 実際に書き込むデータリスト
		List<String> wdata = new ArrayList<String>();

		for (Entry<String, String[]> hc : PVPResultMap.entrySet()) {
			String name = hc.getKey();
			String[] data = hc.getValue();
			// 書き込むリストに追加
			wdata.add(name + "," + data[1] + "," + data[2] + "," + data[3]);
		}

		try {
			datafile.writeLines(wdata);
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	public static Map<String, String[]> getPVPData() {
		return PVPResultMap;
	}

	public static void addWin(Player player) {

		if (getPVPData().containsKey(player.getName())) {
			// 既存
			String[] data = PVPResultMap.get(player.getName());
			int win = Integer.parseInt(data[1]);
			int ren = Integer.parseInt(data[3]);
			win++;
			ren++;
			data[1] = String.valueOf(win);
			data[3] = String.valueOf(ren);
			PVPResultMap.put(player.getName(), data);
			saveData();
		} else {
			// 新規
			String[] data = { player.getName(), "1", "0", "1" };
			PVPResultMap.put(player.getName(), data);
			saveData();
		}
	}

	public static void addLose(Player player) {
		if (getPVPData().containsKey(player.getName())) {
			// 既存
			String[] data = PVPResultMap.get(player.getName());
			int lose = Integer.parseInt(data[2]);
			lose++;
			data[2] = String.valueOf(lose);
			data[3] = "0";
			PVPResultMap.put(player.getName(), data);
			saveData();
		} else {
			// 新規
			String[] data = { player.getName(), "0", "1", "0" };
			PVPResultMap.put(player.getName(), data);
			saveData();
		}
	}

	public static void join(Player toPlayer, Player fromPlayer) {

		player1 = toPlayer;
		player2 = fromPlayer;

		oldLocation1 = player1.getLocation();
		oldLocation2 = player2.getLocation();

		String world1 = plugin.getConfig().getString("PVP_SPAWN_1.world");
		int x1 = plugin.getConfig().getInt("PVP_SPAWN_1.x");
		int y1 = plugin.getConfig().getInt("PVP_SPAWN_1.y");
		int z1 = plugin.getConfig().getInt("PVP_SPAWN_1.z");
		String world2 = plugin.getConfig().getString("PVP_SPAWN_2.world");
		int x2 = plugin.getConfig().getInt("PVP_SPAWN_2.x");
		int y2 = plugin.getConfig().getInt("PVP_SPAWN_2.y");
		int z2 = plugin.getConfig().getInt("PVP_SPAWN_2.z");

		player1.teleport(new Location(plugin.getServer().getWorld(world1), x1, y1, z1));
		player2.teleport(new Location(plugin.getServer().getWorld(world2), x2, y2, z2));
		player1.teleport(new Location(plugin.getServer().getWorld(world1), x1, y1, z1));
		player2.teleport(new Location(plugin.getServer().getWorld(world2), x2, y2, z2));

	}

	public static Player endGame(Player player, boolean b, List<ItemStack> list) {
		Player winner = null;
		Player lose = null;
		try {
			player1.teleport(oldLocation1);
		} catch (Exception e) {
		}
		try {
			player2.teleport(oldLocation2);
		} catch (Exception e) {
		}

		boolean isOnline = true;
		if (b) {
			// 脱退した場合
			if (player1.getName().equals(player.getName())) {
				isOnline = player2.isOnline();
				winner = player1;
				lose = player2;
			} else {
				isOnline = player1.isOnline();
				winner = player2;
				lose = player1;
			}
		}

		if (isOnline) {
			if (player1.getName().equals(player.getName())) {
				addLose(player1);
				addWin(player2);
				winner = player2;
				lose = player1;
				plugin.sendAllPlayerMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ").append(
						ChatColor.GREEN).append("勝者：").append(ChatColor.RED).append(player2.getName()).append(
						ChatColor.GREEN).append("敗者：").append(ChatColor.RED).append(player1.getName()).toString());

			} else {
				addLose(player2);
				addWin(player1);
				winner = player1;
				lose = player2;
				plugin.sendAllPlayerMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ").append(
						ChatColor.GREEN).append("勝者：").append(ChatColor.RED).append(player1.getName()).append(
						ChatColor.GREEN).append("敗者：").append(ChatColor.RED).append(player2.getName()).toString());
			}
		} else {
			addLose(lose);
			addWin(winner);
			plugin.sendAllPlayerMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ").append(
					ChatColor.GREEN).append("勝者：").append(ChatColor.RED).append(winner.getName()).append(
					ChatColor.GREEN).append("敗者：").append(ChatColor.RED).append(lose.getName()).toString());

		}

		if (!b) {
			// 倒したとき

			//勝利者のアイテム全回復
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "repairall " + winner.getName());

			//インベントリインスタンス作成
			Inventory inventory = Bukkit.getServer().createInventory(null, InventoryType.PLAYER);
			//死んだやつのアイテムをインベントリに移動
			for(ItemStack item : list){
				inventory.addItem(item);
			}
			//勝者の画面にインベントリを表示
			winner.openInventory(inventory);
			winner.sendMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ").append(
					ChatColor.RED).append("/pvp inv ").append(ChatColor.GREEN).append("でインベントリを再表示できます。").toString());
			winnerMap.put(winner.getName(), inventory);

		}

		sendPlayerInfo(player1);
		sendPlayerInfo(player2);
		pvpRequestFromTo.clear();
		pvpRequestToFrom.clear();
		player1 = null;
		player2 = null;
		return winner;
	}


	private static void sendPlayerInfo(Player player) {
		String[] data = PVPResultMap.get(player.getName());
		plugin.sendAllPlayerMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ").append(ChatColor.GREEN)
				.append("戦績：").append(ChatColor.RED).append(player.getName()).append(ChatColor.BLUE).append(
						data[1] + "勝、").append(ChatColor.RED).append(data[2] + "負、").append(ChatColor.GOLD).append(
						data[3] + "連勝中").toString());
	}

	/**
	 * PVPリクエストを返却する
	 *
	 * @return
	 */
	public static HashMap<String, String> getPVPRequestFromTo() {
		return pvpRequestFromTo;
	}

	public static HashMap<String, String> getPVPRequestToFrom() {
		return pvpRequestToFrom;
	}

	public static boolean isPlayer(Player pDead) {
		try {
			if (pDead.getName().equals(player1.getName())) {
				return true;
			} else if (pDead.getName().equals(player2.getName())) {
				return true;
			}

		} catch (Exception e) {
			return false;
		}

		return false;
	}

	public static void request(String name, String name2) {
		pvpRequestFromTo.put(name, name2);
		pvpRequestToFrom.put(name2, name);
	}

	private static Class<?> class_CraftInventoryCustom;
	private static String versionPrefix = "";
	static
	{
		// Find classes Bukkit hides from us. :-D
		// Much thanks to @DPOHVAR for sharing the PowerNBT code that powers the reflection approach.
		try {
			String className = Bukkit.getServer().getClass().getName();
			String[] packages = className.split("\\.");
			if (packages.length == 5) {
				versionPrefix = packages[3] + ".";
			}

			class_CraftInventoryCustom = fixBukkitClass("org.bukkit.craftbukkit.inventory.CraftInventoryCustom");
		}
		catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	private static Class<?> fixBukkitClass(String className) {
		className = className.replace("org.bukkit.craftbukkit.", "org.bukkit.craftbukkit." + versionPrefix);
		className = className.replace("net.minecraft.server.", "net.minecraft.server." + versionPrefix);
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static Inventory createInventory(InventoryHolder holder, final int size) {
		Inventory inventory = null;
		try {
			Constructor<?> inventoryConstructor = class_CraftInventoryCustom.getConstructor(InventoryHolder.class, Integer.TYPE);
			inventory = (Inventory)inventoryConstructor.newInstance(holder, size);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return inventory;
	}

	public static Inventory getWinnerInventory(Player player){
		return winnerMap.get(player.getName());
	}

	public static void removeWinnerInventory(Player player) {
		if(winnerMap.containsKey(player.getName())){
			winnerMap.remove(player.getName());
		}
	}
}
