package hijiki_seaweed.Rotation_Hotbar;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.connorlinfoot.actionbarapi.ActionBarAPI;

public class Main extends JavaPlugin implements Listener {

	// 連打防止
	private static Map<Player, Long> AntiClickSpamDelay = new HashMap<Player, Long>();

	public void onEnable()
	{
		this.saveDefaultConfig();
		ConsoleCommandSender c = Bukkit.getConsoleSender();
		FileConfiguration Config = getConfig();

		if( !Config.isSet("RotationMessage") )
		{
			getConfig().set("RotationMessage", "ホットバーをローテーションしました。");
			c.sendMessage("config.yml に RotationMessage が存在しなかったため追加しました。");
		}

		if( !Config.isSet("AntiClickSpamDelay") )
		{
			getConfig().set("AntiClickSpamDelay", 50);
			c.sendMessage("config.yml に AntiClickSpamDelay が存在しなかったため追加しました。");
		}

		saveConfig();

		Bukkit.getPluginManager().registerEvents(this, this);
	}

	public void onDisable()
	{
		// null
	}

	@EventHandler
	(
		priority = EventPriority.HIGHEST
	)
	public void onSwapHandItem(final PlayerSwapHandItemsEvent e)
	{
		Player Player = e.getPlayer();

		// スニークしているか
		boolean isSneaking = Player.isSneaking();

		// スニークしている場合
		if( isSneaking )
		{

			// イベントをキャンセル
			e.setCancelled(true);

			// 連打を防止
			Long Now = System.currentTimeMillis();
			int MinDelay = getConfig().getInt("AntiClickSpamDelay");
			Long CoolDownUntil = AntiClickSpamDelay.get(Player);
			if( MinDelay > 0 )
			{
				if( CoolDownUntil != null && CoolDownUntil > Now )
				{
					return;
				}
				else
				{
					AntiClickSpamDelay.put(Player, Now+MinDelay);
				}
			}

			// インベントリ
			Inventory Inventory = Player.getInventory();

			// インベントリの全アイテム配列
			ItemStack Items[] = new ItemStack[36];
			for( int i = 0; i < Items.length; i++ )
			{
				Items[i] = Inventory.getItem(i);
				if( Items[i] == null )
				{
					Items[i] = new ItemStack(Material.AIR);
				}
			}

			// インベントリを 1 行下にずらす
			for( int i = 0; i < Items.length; i++ )
			{
				// ホットバー行の場合
				if( i > 8 )
				{
					Inventory.setItem(i, Items[i-9]);
				}
				else
				{
					Inventory.setItem(i, Items[27+i]);
				}

			}

			// サウンド再生
			World World = Player.getWorld();
			World.playSound(Player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);

			// メッセージ表示
			String RotationMessage = getConfig().getString("RotationMessage");
			if( RotationMessage.length() > 0 )
			{
				// ActionBarAPI が Load されている場合
				if( getServer().getPluginManager().getPlugin("ActionBarAPI") != null )
				{
					ActionBarAPI.sendActionBar(Player, format(RotationMessage));
				}
				// Load されていない場合
				else
				{
					Player.sendMessage(format(RotationMessage));
				}
			}
		}
		return;
	}

	private static String format(String format)
	{
		return ChatColor.translateAlternateColorCodes('&', format);
	}
}
