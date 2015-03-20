/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jlh.epicCastle.command;

import static es.jlh.epicCastle.handlers.InventoryClick.POS_DIAMOND;
import static es.jlh.epicCastle.handlers.InventoryClick.POS_GOLD;
import static es.jlh.epicCastle.handlers.InventoryClick.POS_MONEY;
import static es.jlh.epicCastle.handlers.InventoryClick.POS_XP;
import es.jlh.epicCastle.plugin.EpicCastle;
import static es.jlh.epicCastle.plugin.EpicCastle.PLUGIN;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEDIAMOND;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEGOLD;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEMONEY;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEXP;
import static es.jlh.epicCastle.plugin.EpicCastleManager.OWNER;
import es.jlh.epicCastle.utils.Lang;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Julian
 */
public class InfoCommandExecutor implements CommandExecutor{
    private final EpicCastle plugin;
    
    public InfoCommandExecutor(EpicCastle pl) {
        this.plugin = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String args[]) {                
        if(args.length == 0) {
            sender.sendMessage(PLUGIN + ChatColor.YELLOW + "v" + plugin.getDescription().getVersion());
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "###################");
            sender.sendMessage(ChatColor.YELLOW + "#" + ChatColor.AQUA +" Lista de comandos " + ChatColor.YELLOW + "#");            
            sender.sendMessage(ChatColor.YELLOW + "###################");           
            sender.sendMessage("");         
            sender.sendMessage(ChatColor.AQUA + "/ece castillos     ");
            
            /* Casos con permiso */
            if (sender.hasPermission("ec.info")) {
                sender.sendMessage(ChatColor.AQUA + "/ece info     ");
            }
            
            if (sender.hasPermission("ec.settp")) {
                sender.sendMessage(ChatColor.AQUA + "/ece settp <xp|money|gold|diamond>     ");
            }
            
            if (sender.hasPermission("ec.reload")) {
                sender.sendMessage(ChatColor.AQUA + "/ece reload     ");
            }
            
            sender.sendMessage("");            
            sender.sendMessage(ChatColor.YELLOW + "###################");            
            sender.sendMessage("");            
            sender.sendMessage(EpicCastle.PLUGIN + ChatColor.GOLD + "Creado por Julito");
            return true;
        }
        
        if (args.length >= 1) {            
            if (args[0].compareToIgnoreCase("info") == 0) {
                if (!sender.hasPermission("ec.info")) {
                    sender.sendMessage(EpicCastle.PLUGIN + Lang.PLUGIN_NO_PERMISSION.getText());
                    return true;
                }
                sender.sendMessage(EpicCastle.PLUGIN + ChatColor.YELLOW + "\nSimplemente crea una zona con WorldGuard:"
                        + ChatColor.BLUE + "\n- castleXP\n- castleGold\n- castleDiamond\n- castleMoney"
                        + ChatColor.YELLOW + "\nDentro de la zona crea un cartel con las siguientes lineas:"
                        + ChatColor.BLUE + "\n- EpicCastle\n- Tiempo conquista (En segundos)\n- Tiempo recompensa (En segundos)");
                return true;
            }
            
            if (args[0].compareToIgnoreCase("castillos") == 0) {                
                // Esto no puede ser ejecutado por la consola
                if (!(sender instanceof Player)) {
                    sender.sendMessage(EpicCastle.PLUGIN + ChatColor.RED + "Consola no puedes hacer eso");
                    return true;
                }
                
                Player pl = (Player)sender;
                Inventory i = Bukkit.getServer().createInventory(null, 9, "Info de los castillos");
                
                createDisplay(Material.EXP_BOTTLE, i, POS_XP, "Castillo XP", 
                        "Dueño actual: " + ((plugin.getManager().valorSign(CASTLEXP, OWNER) == null) ? 
                                "ninguno":plugin.getManager().valorSign(CASTLEXP, OWNER)));
                createDisplay(Material.GOLD_NUGGET, i, POS_MONEY, "Castillo Money", 
                        "Dueño actual: " + ((plugin.getManager().valorSign(CASTLEMONEY, OWNER) == null) ? 
                                "ninguno":plugin.getManager().valorSign(CASTLEMONEY, OWNER)));
                createDisplay(Material.GOLD_BLOCK, i, POS_GOLD, "Castillo Oro", 
                        "Dueño actual: " + ((plugin.getManager().valorSign(CASTLEGOLD, OWNER) == null) ? 
                                "ninguno":plugin.getManager().valorSign(CASTLEGOLD, OWNER)));
                createDisplay(Material.DIAMOND_BLOCK, i, POS_DIAMOND, "Castillo Diamante", 
                        "Dueño actual: " + ((plugin.getManager().valorSign(CASTLEDIAMOND, OWNER) == null) ? 
                                "ninguno":plugin.getManager().valorSign(CASTLEDIAMOND, OWNER)));

                pl.openInventory(i);
                
                return true;
            }
            
            if (args[0].compareToIgnoreCase("settp") == 0) {                                
                if (!(sender instanceof Player)) {
                    sender.sendMessage(EpicCastle.PLUGIN + Lang.PLUGIN_NO_PERMISSION.getText());
                    return true;
                }
                
                Player pl = (Player)sender;
                if (!pl.hasPermission("ec.settp")) {
                    pl.sendMessage(EpicCastle.PLUGIN + Lang.PLUGIN_NO_PERMISSION.getText());
                    return true;                    
                }
                
                if (args[1] != null) {
                    if (args[1].compareToIgnoreCase("xp") == 0) {
                        plugin.getManager().registraTp(pl.getLocation(), "castleXP");
                    }
                    else if (args[1].compareToIgnoreCase("money") == 0) {
                        plugin.getManager().registraTp(pl.getLocation(), "castleMoney");
                    }
                    else if (args[1].compareToIgnoreCase("gold") == 0) {
                        plugin.getManager().registraTp(pl.getLocation(), "castleGold");
                    }
                    else if (args[1].compareToIgnoreCase("diamond") == 0) {
                        plugin.getManager().registraTp(pl.getLocation(), "castleDiamond");
                    }
                    else {
                        return false;
                    }
                    
                    pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_TELEPORT_REGISTER.getText());
                    
                    plugin.getManager().onDisable();
                    plugin.getManager().onEnable();
                    
                    return true;
                }
                return true;            
            }
            
            if (args[0].compareToIgnoreCase("reload") == 0) {         
                if (sender instanceof Player) {
                    Player pl = (Player) sender;
                    if (!pl.hasPermission("ec.reload")) {
                        pl.sendMessage(EpicCastle.PLUGIN + Lang.PLUGIN_NO_PERMISSION.getText());
                        return true;
                    }
                }
                plugin.getManager().onDisable();
                plugin.getManager().onEnable();
                sender.sendMessage(EpicCastle.PLUGIN + Lang.PLUGIN_RELOAD.getText());
                return true;
            }            
        }        
        return false;
    }
    
    public static void createDisplay(Material material, Inventory inv, int Slot, String name, String lore) {        
        ItemStack item = new ItemStack(material);        
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        ArrayList<String> Lore = new ArrayList();
        Lore.add(lore);
        
        meta.setLore(Lore);        
        item.setItemMeta(meta);
        inv.setItem(Slot, item);
    }
}
