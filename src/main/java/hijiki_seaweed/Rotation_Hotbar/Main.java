package hijiki_seaweed.Rotation_Hotbar;

import java.util.HashMap;
import java.util.Map;

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

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/*
 * Main クラス
 */
public class Main extends JavaPlugin implements Listener {

	/*
	 * メンバー変数の宣言
	 */
	private Map<Player, Long> antiClickSpamDelay = new HashMap<Player, Long>(); // 連打防止用

	/*
	 * プラグインが Load されたとき
	 */
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		ConsoleCommandSender console = this.getServer().getConsoleSender();
		FileConfiguration config = this.getConfig();

		if( !config.isSet( "RotationMessage" ) ) {
			this.getConfig().set( "RotationMessage", "&7&lホットバーをローテーションしました。" );
			console.sendMessage( "config.yml に RotationMessage が存在しなかったため追加しました。" );
		}

		if( !config.isSet( "AntiClickSpamDelay" ) ) {
			this.getConfig().set( "AntiClickSpamDelay", 50 );
			console.sendMessage( "config.yml に AntiClickSpamDelay が存在しなかったため追加しました。" );
		}

		this.saveConfig();

		this.getServer().getPluginManager().registerEvents( this, this );
	}

	/*
	 * プレイヤーがメインハンドとオフハンドのアイテムを入れ替えた時 (F キー)
	 */
	@EventHandler( priority = EventPriority.HIGHEST )
	public void onSwapHandItem( PlayerSwapHandItemsEvent e ) {
		Player player = e.getPlayer();

		// スニークしているか
		boolean isSneaking = player.isSneaking();

		// スニークしている場合
		if( isSneaking ) {
			// イベントをキャンセル
			e.setCancelled( true );

			// 連打を防止
			Long now = System.currentTimeMillis();
			int minDelay = getConfig().getInt( "AntiClickSpamDelay" );
			Long coolDownUntil = this.antiClickSpamDelay.get( player );
			if( minDelay > 0 ) {
				if( coolDownUntil != null && coolDownUntil > now ) {
					return;
				} else {
					this.antiClickSpamDelay.put( player, now + minDelay );
				}
			}

			// インベントリ
			Inventory inventory = player.getInventory();

			// インベントリの全アイテム配列
			ItemStack items[] = new ItemStack[36];
			for( int i = 0; i < items.length; i++ ) {
				items[i] = inventory.getItem( i );
				if( items[i] == null ) {
					items[i] = new ItemStack( Material.AIR );
				}
			}

			// インベントリを 1 行下にずらす
			for( int i = 0; i < items.length; i++ ) {
				// ホットバー行の場合
				if( i > 8 ) {
					inventory.setItem( i, items[i - 9] );
				} else {
					inventory.setItem( i, items[27 + i] );
				}
			}

			// サウンド再生
			World world = player.getWorld();
			world.playSound( player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f );

			// メッセージ表示
			String rotationMessage = this.format( getConfig().getString( "RotationMessage" ) );
			if( rotationMessage.length() > 0 ) {
				this.sendActionBar( player, rotationMessage );
			}
		}
		return;
	}

	/*
	 * 文字列を Minecraft 内で使用できる形に整形する
	 */
	private String format( String format ) {
		return ChatColor.translateAlternateColorCodes( '&', format );
	}

	/*
	 * 指定した player の ActionBar に Message を送信する
	 */
	private void sendActionBar( Player player, String message ) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
	}
}
