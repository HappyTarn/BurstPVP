package com.happytarn.burstPVP.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.happytarn.burstPVP.Main;
import com.happytarn.burstPVP.PVPD;
import com.happytarn.burstPVP.PVPData;
import com.happytarn.burstPVP.util.PVPComparatorKD;
import com.happytarn.burstPVP.util.PVPComparatorLose;
import com.happytarn.burstPVP.util.PVPComparatorWin;

public class BurstPVPCommand implements CommandExecutor {

	public final Logger log = Main.log;
	private Main plugin;

	/**
	 * デフォルトコンストラクタ
	 *
	 * @param plugin
	 */
	public BurstPVPCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		if (commandLabel.equalsIgnoreCase("pvp")) {
			if (sender instanceof Player) {
			} else {
				return false;
			}
			Player player = (Player) sender;
			Player toFight;
			if (args.length == 0) {
				if (sender instanceof Player) {
					sender.sendMessage((new StringBuilder()).append(ChatColor.GOLD).append("Developed by : ").append(
							ChatColor.RED).append("HappyTarn.  /pvp help でコマンドを確認してください。").toString());
					return true;
				}
			}

			if (args.length >= 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))) {
				sender.sendMessage("WIKIみろカス");
				return true;
			}

			// check
			if (args.length >= 1 && args[0].equals("check")) {
				String[] data = PVPData.getPVPData().get(player.getName());
				if (data == null) {

				} else {
					player.sendMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ").append(
							ChatColor.GREEN).append("戦績：").append(ChatColor.RED).append(player.getName()).append(
							ChatColor.BLUE).append(data[1] + "勝、").append(ChatColor.RED).append(data[2] + "負、").append(
							ChatColor.GOLD).append(data[3] + "連勝中").toString());
				}
				return true;
			}

			if (args.length >= 1 && args[0].equals("leave")) {
				if (plugin.isInGame()) {

					if (PVPData.isPlayer(player)) {
						PVPData.endGame(player, true, null);
						plugin.setGame(false);
					}

				}
				return true;
			}

			if(args.length >= 1 && (args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("inv"))){

				Inventory inventory = PVPData.getWinnerInventory(player);

				if(inventory == null){
					player.sendMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ").append(
							ChatColor.GREEN).append("受け取れるアイテムはありません。").toString());
				}else{
					PVPData.removeWinnerInventory(player);
					player.openInventory(inventory);
				}

				return true;
			}

			if (args.length >= 1 && args[0].equals("rank")) {
				if (args.length == 1) {
					sender.sendMessage(new StringBuffer().append(ChatColor.RED).append("/pvp rank (KD|Win|Lose)")
							.toString());
					return true;
				}
				ArrayList<PVPD> dataList = new ArrayList<>();
				for (String[] list : PVPData.getPVPData().values()) {
					PVPD p = new PVPD(list[0], list[1], list[2]);
					dataList.add(p);
				}
				boolean cmdFlg = false;
				if (args[1].equalsIgnoreCase("kd")) {
					Collections.sort(dataList, new PVPComparatorKD());
					cmdFlg = true;
				}
				if (args[1].equalsIgnoreCase("win")) {
					Collections.sort(dataList, new PVPComparatorWin());
					cmdFlg = true;
				}
				if (args[1].equalsIgnoreCase("lose")) {
					Collections.sort(dataList, new PVPComparatorLose());
					cmdFlg = true;
				}

				if (cmdFlg) {

					int max = dataList.size() >= 10? 10:dataList.size();

					player.sendMessage("------------------- PVP RANK -------------------");
					for (int i = 0; i < max; i++) {
						player.sendMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ").append(
								ChatColor.GREEN).append((i + 1) + "位：").append(ChatColor.RED).append(
								dataList.get(i).getName()).append(ChatColor.WHITE).append("：").append(ChatColor.BLUE)
								.append(dataList.get(i).getWin() + "勝").append(ChatColor.WHITE).append("：").append(
										ChatColor.RED).append(dataList.get(i).getLose() + "負").append(ChatColor.WHITE)
								.append("：").append(ChatColor.GOLD).append("KD：" + dataList.get(i).getKd()).toString());
					}

				} else {
					sender.sendMessage(new StringBuffer().append(ChatColor.RED).append("/pvp rank (KD|Win|Lose)")
							.toString());
				}

				return true;
			}

			// accept
			if (args.length >= 1 && (args[0].equalsIgnoreCase("accept"))) {
				if (plugin.isInGame()) {
					sender.sendMessage(new StringBuffer().append(ChatColor.RED).append("他の人が戦っています。").toString());
					return true;
				} else {
					if (PVPData.getPVPRequestToFrom().get(player.getName()) == null) {
						sender.sendMessage(new StringBuffer().append(ChatColor.RED).append("誰もあなたに申請していないです。")
								.toString());
						return true;
					} else {
						// 申し込まれてた場合

						// 申し込みリストから逆引き
						String toName = PVPData.getPVPRequestFromTo().get(
								PVPData.getPVPRequestToFrom().get(player.getName()));

						if (player.getName().equals(toName)) {
							// 申し込みリストのToと自分の名前が一致する場合
							Player fromPlayer = plugin.getServer().getPlayer(
									PVPData.getPVPRequestToFrom().get(player.getName()));
							if (fromPlayer.isOnline() && !fromPlayer.isDead()) {
								// バトル開始
								PVPData.join(player, fromPlayer);
								plugin.sendAllPlayerMessage(new StringBuffer().append(ChatColor.GOLD).append("[1vs1] ")
										.append(ChatColor.RED).append(fromPlayer.getName()).append(ChatColor.GREEN)
										.append(" vs ").append(ChatColor.RED).append(player.getName()).append(
												ChatColor.GREEN).append("が始まりました。").toString());
								plugin.setGame(true);
								return true;
							} else {
								sender.sendMessage(new StringBuffer().append(ChatColor.RED).append(
										"申し込みしてきた人がオフラインまたは死んでます。").toString());
								return true;
							}
						} else {
							// 一致しない場合
							sender.sendMessage(new StringBuffer().append(ChatColor.RED).append(
									"申し込みしてきた人が既に別の人に申し込みしました。").toString());
							return true;
						}
					}
				}
			}
			// set
			if (args.length >= 1 && args[0].equals("set")) {
				if (args.length >= 2 && args[1].equals("1") && sender.isOp()) {
					int x = player.getLocation().getBlockX();
					int y = player.getLocation().getBlockY();
					int z = player.getLocation().getBlockZ();
					String world = player.getLocation().getWorld().getName();
					plugin.getConfig().set("PVP_SPAWN_1.world", world);
					plugin.getConfig().set("PVP_SPAWN_1.x", x);
					plugin.getConfig().set("PVP_SPAWN_1.y", y);
					plugin.getConfig().set("PVP_SPAWN_1.z", z);
					plugin.saveConfig();
					player.sendMessage((new StringBuilder()).append(ChatColor.RED).append(
							"Spawnpoint 1 has been set at X:").append(ChatColor.GOLD).append(x).append(ChatColor.RED)
							.append(", Y:").append(ChatColor.GOLD).append(y).append(ChatColor.RED).append(", Z:")
							.append(ChatColor.GOLD).append(z).toString());
				} else if (args.length >= 2 && args[1].equals("2") && sender.isOp()) {
					int x = player.getLocation().getBlockX();
					int y = player.getLocation().getBlockY();
					int z = player.getLocation().getBlockZ();
					String world = player.getLocation().getWorld().getName();
					plugin.getConfig().set("PVP_SPAWN_2.world", world);
					plugin.getConfig().set("PVP_SPAWN_2.x", x);
					plugin.getConfig().set("PVP_SPAWN_2.y", y);
					plugin.getConfig().set("PVP_SPAWN_2.z", z);
					plugin.saveConfig();
					player.sendMessage((new StringBuilder()).append(ChatColor.RED).append(
							"Spawnpoint 2 has been set at X:").append(ChatColor.GOLD).append(x).append(ChatColor.RED)
							.append(", Y:").append(ChatColor.GOLD).append(y).append(ChatColor.RED).append(", Z:")
							.append(ChatColor.GOLD).append(z).toString());
				} else {
					sender.sendMessage(new StringBuffer().append(ChatColor.RED).append("Usage : ").append(
							ChatColor.DARK_RED).append("[OP]/pvp set (1 or 2)").toString());
				}
				return true;
			}

			// 申し込み
			if (args.length >= 1 && plugin.getServer().getPlayer(args[0]) != null) {
				toFight = plugin.getServer().getPlayer(args[0]);
				if (toFight.isOnline()) {
					if (!"127.0.0.1".equals(player.getAddress().getHostName().toString())
							&& player.getAddress().getHostName().toString().equals(
									toFight.getAddress().getHostName().toString())) {
						sender.sendMessage(new StringBuffer().append(ChatColor.RED).append("このプレイヤーとは戦えません。")
								.toString());
						return true;
					} else {
						PVPData.request(player.getName(), toFight.getName());
						toFight.sendMessage(new StringBuffer().append(ChatColor.RED).append(player.getName()).append(
								ChatColor.AQUA).append("から戦いの申し込みがあります。").append(ChatColor.DARK_GREEN).append(
								"/pvp accept").append(ChatColor.AQUA).append("で戦いを承諾します。").toString());
						player.sendMessage(new StringBuffer().append(ChatColor.RED).append(toFight.getName()).append(
								ChatColor.AQUA).append("に戦いの申し込みをしました。").toString());
					}
				} else {
					sender.sendMessage(new StringBuffer().append(ChatColor.RED).append("プレイヤーが存在しません。").toString());
					return true;
				}

			} else if (args.length >= 1 && plugin.getServer().getPlayer(args[0]) == null) {
				sender.sendMessage(new StringBuffer().append(ChatColor.RED).append("プレイヤーが存在しません。").toString());
				return true;
			}

			return true;
		}

		if (commandLabel.equalsIgnoreCase("burstpvp") || commandLabel.equalsIgnoreCase("bp")) {
			// 引数なし
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					// 権限チェック
					if (player.hasPermission("hapitanCmd.burstpvp.admin")) {
						player.sendMessage("引数無しでは実行できません。/bp help で確認してください。");
						return true;
					} else {
						player.sendMessage("権限がありません。hapitanCmd.burstpvp.admin");
						return true;
					}
				} else {
					sender.sendMessage("引数無しでは実行できません。/bp help で確認してください。");
					return true;
				}
			}

			// burstpvp reload
			if (args.length >= 1 && (args[0].equalsIgnoreCase("reload"))) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					// 権限チェック
					if (!player.hasPermission("hapitanCmd.burstpvp.admin")) {
						player.sendMessage("権限がありません。hapitanCmd.burstpvp.admin");
						return true;
					}
					plugin.loadConfigFile();
					sender.sendMessage(new StringBuffer().append(ChatColor.GOLD).append("リロード完了！").toString());
					return true;
				} else {
					sender.sendMessage("引数無しでは実行できません。/bp help で確認してください。");
					return true;
				}
			}

			// burstpvp reload
			if (args.length >= 1 && (args[0].equalsIgnoreCase("start"))) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					// 権限チェック
					if (!player.hasPermission("hapitanCmd.burstpvp.admin")) {
						player.sendMessage("権限がありません。hapitanCmd.burstpvp.admin");
						return true;
					}

					// 開始の合図
					plugin.sendAllPlayerMessage(new StringBuffer().append(ChatColor.RED).append("突発PvPイベント開始！！")
							.toString());

					// 全プレイヤーを指定の位置に移動
					plugin.movePlayer(player);

					plugin.burstPVPstart();

					return true;
				} else {
					sender.sendMessage("引数無しでは実行できません。/bp help で確認してください。");
					return true;
				}
			}

			// burstpvp reload
			if (args.length >= 1 && (args[0].equalsIgnoreCase("stop"))) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					// 権限チェック
					if (!player.hasPermission("hapitanCmd.burstpvp.admin")) {
						player.sendMessage("権限がありません。hapitanCmd.burstpvp.admin");
						return true;
					}

					// 開始の合図
					plugin.sendAllPlayerMessage(new StringBuffer().append(ChatColor.RED).append("突発PvPイベント終了！！")
							.toString());

					plugin.burstPVPend();
					return true;
				} else {
					sender.sendMessage("引数無しでは実行できません。/bp help で確認してください。");
					return true;
				}
			}

			// burstpvp reload
			if (args.length >= 1 && (args[0].equalsIgnoreCase("check"))) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					// 権限チェック
					if (!player.hasPermission("hapitanCmd.burstpvp.admin")) {
						player.sendMessage("権限がありません。hapitanCmd.burstpvp.admin");
						return true;
					}
					plugin.check(player);
					return true;
				} else {
					sender.sendMessage("引数無しでは実行できません。/bp help で確認してください。");
					return true;
				}
			}

			// burstpvp reload
			if (args.length >= 1 && (args[0].equalsIgnoreCase("clear"))) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					// 権限チェック
					if (!player.hasPermission("hapitanCmd.burstpvp.admin")) {
						player.sendMessage("権限がありません。hapitanCmd.burstpvp.admin");
						return true;
					}

					plugin.clearLocation();
					player.sendMessage(new StringBuffer().append(ChatColor.RED).append("開始位置の設定をクリアしました。").append(
							plugin.getLocationMapSize()).toString());
					player.sendMessage(new StringBuffer().append(ChatColor.RED).append("現在の開始位置個数：").append(
							plugin.getLocationMapSize()).toString());
					return true;
				} else {
					sender.sendMessage("引数無しでは実行できません。/bp help で確認してください。");
					return true;
				}
			}

			// burstpvp reload
			if (args.length >= 2 && (args[0].equalsIgnoreCase("set"))) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					// 権限チェック
					if (!player.hasPermission("hapitanCmd.burstpvp.admin")) {
						player.sendMessage("権限がありません。hapitanCmd.burstpvp.admin");
						return true;
					}

					plugin.setLocation(args[1], ((Player) sender).getLocation());
					player.sendMessage(new StringBuffer().append(ChatColor.YELLOW).append("現在の位置を開始位置として設定しました。")
							.toString());
					player.sendMessage(new StringBuffer().append(ChatColor.RED).append("現在の開始位置個数：").append(
							plugin.getLocationMapSize()).toString());

					return true;
				} else {
					sender.sendMessage("引数無しでは実行できません。/bp help で確認してください。");
					return true;
				}
			}

			if (args.length >= 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h"))) {
				sender.sendMessage("作者に聞いてください");
				return true;
			}
		}
		return false;
	}
}
