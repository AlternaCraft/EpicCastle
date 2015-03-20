/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jlh.epicCastle.handlers;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import es.jlh.epicCastle.plugin.EpicCastle;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEDIAMOND;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEGOLD;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEMONEY;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEXP;
import static es.jlh.epicCastle.plugin.EpicCastleManager.OWNER;
import static es.jlh.epicCastle.plugin.EpicCastleManager.TICKS;
import es.jlh.epicCastle.utils.Lang;
import es.jlh.epicCastle.utils.PlayerEffect;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Julian
 */
public class PlayerMessageCastle implements Listener, EventExecutor {
    
    private final EpicCastle plugin;    
    private final WorldGuardPlugin wgplugin;
    private final HashMap<String, String> owner = new HashMap();
    
    public static final HashMap<Player, String> visitantes = new HashMap();
    public static final ArrayList<PlayerEffect> chetados = new ArrayList();
    
    public static final PotionEffect vel = new PotionEffect(PotionEffectType.SPEED,TICKS * 300, 1);
    public static final PotionEffect salto = new PotionEffect(PotionEffectType.JUMP, TICKS * 300, 1);
    public static final PotionEffect fuerza = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, TICKS * 300, 1);
    public static final PotionEffect reg = new PotionEffect(PotionEffectType.REGENERATION, TICKS * 300, 1);
    
    public PlayerMessageCastle(EpicCastle plugin) {
        this.plugin = plugin;
        this.wgplugin = this.plugin.getWorldguard();               
    }

    @EventHandler
    public void mensajeZona(PlayerMoveEvent e) {
        Player pl = e.getPlayer();

        owner.clear();
        owner.put("castleXP", plugin.getManager().valorSign(CASTLEXP, OWNER));
        owner.put("castleGold", plugin.getManager().valorSign(CASTLEGOLD, OWNER));
        owner.put("castleMoney", plugin.getManager().valorSign(CASTLEDIAMOND, OWNER));
        owner.put("castleDiamond", plugin.getManager().valorSign(CASTLEMONEY, OWNER));        

        // Objeto para obtener las zonas del world guard de un mundo
        RegionManager regionManager = wgplugin.getRegionManager(pl.getWorld());       

        // Vector de la posicion del jugador
        BlockVector pos = new BlockVector(new Vector (
                pl.getLocation().getBlockX(), 
                pl.getLocation().getBlockY(), 
                pl.getLocation().getBlockZ()));

        if (!visitantes.isEmpty() && visitantes.containsKey(pl)) {
            ProtectedRegion all = regionManager.getRegion(visitantes.get(pl));

            if (!all.contains(pos)) {
                String by = owner.get(visitantes.get(pl));

                if (by == null || by.compareTo("") == 0) {
                    by = "nadie";
                }

                // Compruebo efectos
                for (PlayerEffect pe : chetados) {
                    if (pe.getPl().equals(pl)) {
                        quitaEfectos(pe);
                        break;
                    }
                }
                
                // Mensaje que indica que saliste de la region                
                pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_LEAVE_MESSAGE.getText()
                        .replace("%CASTLE%", plugin.getManager().cambiaNombre(visitantes.get(pl)))
                        .replace("%OWNER%", by)); 
                
                visitantes.remove(pl);                
            }
        }
        else {
            // En caso de que el jugador sea la primera vez que entra empieza aqui
            ProtectedRegion xp = regionManager.getRegion("castleXP");
            ProtectedRegion gold = regionManager.getRegion("castleGold");
            ProtectedRegion diamond = regionManager.getRegion("castleDiamond");
            ProtectedRegion money = regionManager.getRegion("castleMoney");                
            
            if (xp != null && xp.contains(pos)) {            
                //Mensaje que indica que saliste de la region
                String by = this.owner.get("castleXP");
                
                by = ponPlayerEfectos(by, pl);
                
                pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_WELCOME_MESSAGE.getText()
                        .replace("%CASTLE%", Lang.CASTLE_TYPE_XP.getText())
                        .replace("%OWNER%", by));
                
                visitantes.put(pl, "castleXP");
            }
            else if (gold != null && gold.contains(pos)) {
                String by = this.owner.get("castleGold");
                
                by = ponPlayerEfectos(by, pl);
                
                pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_WELCOME_MESSAGE.getText()
                        .replace("%CASTLE%", Lang.CASTLE_TYPE_GOLD.getText())
                        .replace("%OWNER%", by));
                
                visitantes.put(pl, "castleGold");
            }
            else if (diamond != null && diamond.contains(pos)) {
                String by = this.owner.get("castleDiamond");
                
                by = ponPlayerEfectos(by, pl);
                
                pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_WELCOME_MESSAGE.getText()
                        .replace("%CASTLE%", Lang.CASTLE_TYPE_DIAMOND.getText())
                        .replace("%OWNER%", by));
                
                visitantes.put(pl, "castleDiamond");
            }
            else if (money != null && money.contains(pos)) {
                String by = this.owner.get("castleMoney");
                
                by = ponPlayerEfectos(by, pl);
                
                pl.sendMessage(EpicCastle.PLUGIN + Lang.CASTLE_WELCOME_MESSAGE.getText()
                        .replace("%CASTLE%", Lang.CASTLE_TYPE_MONEY.getText())
                        .replace("%OWNER%", by));
                
                visitantes.put(pl, "castleMoney");
            }
        }
    }
    
    public String ponPlayerEfectos(String by, Player pl) {
        if (by == null || by.compareTo("") == 0) {
            by = "nadie";
        }
        else {
            Faction faction = FactionColl.get().getByName(by);                
            if (faction != null && faction.getOnlinePlayers().contains(pl)) {                        
                ponEfectos(pl);
            }
        }
        return by;
    }
    
    public void ponEfectos(Player pl) {
        boolean bvel = false;
        boolean bsalto = false;
        boolean bfuerza = false;
        boolean breg = false;
        
        if (pl.hasPermission("ec.benefit")) {
            if (!pl.hasPotionEffect(PotionEffectType.SPEED)) {
                pl.addPotionEffect(vel);
                bvel = true;
            }                        
            if (!pl.hasPotionEffect(PotionEffectType.JUMP)) {
                pl.addPotionEffect(salto);
                bsalto = true;
            }                        
            if (!pl.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                pl.addPotionEffect(fuerza);
                bfuerza = true;
            }                        
            if (!pl.hasPotionEffect(PotionEffectType.REGENERATION)) {
                pl.addPotionEffect(reg);
                breg = true;
            }
            
            chetados.add(new PlayerEffect(pl, bvel, bsalto, bfuerza, breg));
        }
    }
    
    public void quitaEfectos(PlayerEffect pe) {        
        if (pe.isVel()) {
            pe.getPl().removePotionEffect(PotionEffectType.SPEED);
        }                        
        if (pe.isSalto()) {
            pe.getPl().removePotionEffect(PotionEffectType.JUMP);
        }                        
        if (pe.isFuerza()) {
            pe.getPl().removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        }                        
        if (pe.isReg()) {
            pe.getPl().removePotionEffect(PotionEffectType.REGENERATION);
        }
        chetados.remove(pe);
    } 
    
    @Override
    public void execute(Listener ll, Event event) throws EventException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
