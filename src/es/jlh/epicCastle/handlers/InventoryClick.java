/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jlh.epicCastle.handlers;

import es.jlh.epicCastle.plugin.EpicCastle;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEDIAMOND;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEGOLD;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEMONEY;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEXP;
import es.jlh.epicCastle.utils.Lang;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;

/**
 * Clase evento para comprobar los jugadores que pulsan click en el inv virtual
 * @author Julián
 * @version 1.0
 */
public class InventoryClick implements Listener, EventExecutor  {

    public static final int POS_XP = 1;
    public static final int POS_MONEY = 3;
    public static final int POS_GOLD = 5;
    public static final int POS_DIAMOND = 7;
    
    public static final int TICKS = 20;    
    public static final int TIME = 3;    
    
    private final EpicCastle plugin;
    private final HashMap<Player, String> jugadores = new HashMap();
    
    public InventoryClick(EpicCastle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev) {
        if (jugadores.containsKey(ev.getPlayer())) {
            // Si se mueve cancelo la tarea
            Bukkit.getServer().getScheduler().cancelTask(Integer.valueOf(jugadores.get(ev.getPlayer())));
            jugadores.remove(ev.getPlayer());
            ev.getPlayer().sendMessage(EpicCastle.PLUGIN + Lang.CANCEL_TELEPORT_PLAYER.getText());
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        final Player pl = (Player)event.getWhoClicked();
        final Location loc = getLocation(event.getSlot());
        
        if (inventory.getName().equals("Info de los castillos")) {                    
            event.setCancelled(true);
            
            if (loc != null) {
                pl.closeInventory();
                pl.sendMessage(EpicCastle.PLUGIN + Lang.DELAY_TELEPORT_PLAYER.getText().replace("%TIME%", String.valueOf(TIME)));
                
                jugadores.put(pl, String.valueOf(Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    pl.teleport(loc);
                    pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_TELEPORT_PLAYER.getText());
                    jugadores.remove(pl); // Elimino al jugador del hashmap
                }, TICKS * TIME)));
            }                        
        }
    }        
    
    public Location getLocation(int pos) {
        switch(pos) {
            case POS_XP:
                return plugin.getManager().teleports[CASTLEXP];
            case POS_MONEY:
                return plugin.getManager().teleports[CASTLEMONEY];                
            case POS_GOLD:
                return plugin.getManager().teleports[CASTLEGOLD];                
            case POS_DIAMOND:
                return plugin.getManager().teleports[CASTLEDIAMOND];
            default:
                return null;
        }
    }

    @Override
    public void execute(Listener ll, Event event) throws EventException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
