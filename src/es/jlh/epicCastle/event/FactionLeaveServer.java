/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jlh.epicCastle.event;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import es.jlh.epicCastle.plugin.EpicCastle;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEDIAMOND;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEGOLD;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEMONEY;
import static es.jlh.epicCastle.plugin.EpicCastleManager.CASTLEXP;
import static es.jlh.epicCastle.plugin.EpicCastleManager.OWNER;
import es.jlh.epicCastle.utils.Lang;
import es.jlh.epicCastle.utils.PlayerEffect;
import static es.jlh.epicCastle.event.PlayerMessageCastle.chetados;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Julian
 */
public class FactionLeaveServer implements EventExecutor, Listener {

    private final EpicCastle plugin;
    private final HashMap<String, String> owner = new HashMap(); 
    
    public FactionLeaveServer(EpicCastle pl) {
        this.plugin = pl;
    }
    
    @EventHandler
    public void playerDisconect(PlayerQuitEvent e) {        
        compruebaJugador(e.getPlayer());
        
    }
    
    @EventHandler
    public void playerKick(PlayerKickEvent e) {
        compruebaJugador(e.getPlayer());
    }
    
    public void compruebaJugador(Player pl) {        
        MPlayer uplayer = MPlayer.get(pl);
        Faction other = uplayer.getFaction();
        
        // Compruebo si el jugador estaba conquistando un castillo para terminar el proceso
        if (pl.getMetadata("tiempo") != null) {
            pl.getPlayer().setMetadata("tiempo", new FixedMetadataValue(plugin, 0));
        }
        
        owner.clear();
        owner.put("castleXP", plugin.getManager().valorSign(CASTLEXP, OWNER));
        owner.put("castleGold", plugin.getManager().valorSign(CASTLEGOLD, OWNER));
        owner.put("castleMoney", plugin.getManager().valorSign(CASTLEDIAMOND, OWNER));
        owner.put("castleDiamond", plugin.getManager().valorSign(CASTLEMONEY, OWNER));
        
        if (other.getOnlinePlayers().size() == 1 && other.getOnlinePlayers().
                contains(pl)) {
            String fac = other.getName();

            String xp = plugin.getManager().valorSign(CASTLEXP, OWNER);
            String gold = plugin.getManager().valorSign(CASTLEGOLD, OWNER);
            String diamond = plugin.getManager().valorSign(CASTLEDIAMOND, OWNER);
            String money = plugin.getManager().valorSign(CASTLEMONEY, OWNER);

            if (xp != null && xp.compareToIgnoreCase(fac) == 0) {
                plugin.getManager().carteles[CASTLEXP].setLine(OWNER, "");
                plugin.getManager().carteles[CASTLEXP].update();
                plugin.getManager().cargaCarteles();
                
                for (PlayerEffect pe : chetados) {
                    if (pe.getPl().equals(pl)) {
                        quitaEfectos(pe);
                        break;
                    }
                }
                
                Bukkit.broadcastMessage(EpicCastle.PLUGIN + Lang.CASTLE_RELEASED.getText().replace("%CASTLE%", "Castillo XP"));
            }
            if (gold != null && gold.compareToIgnoreCase(fac) == 0) {
                plugin.getManager().carteles[CASTLEGOLD].setLine(OWNER, "");
                plugin.getManager().carteles[CASTLEGOLD].update();
                plugin.getManager().cargaCarteles();
                
                for (PlayerEffect pe : chetados) {
                    if (pe.getPl().equals(pl)) {
                        quitaEfectos(pe);
                        break;
                    }
                }
                
                Bukkit.broadcastMessage(EpicCastle.PLUGIN + Lang.CASTLE_RELEASED.getText().replace("%CASTLE%", "Castillo Oro"));
            }
            if (diamond != null && diamond.compareToIgnoreCase(fac) == 0) {
                plugin.getManager().carteles[CASTLEDIAMOND].setLine(OWNER, "");
                plugin.getManager().carteles[CASTLEDIAMOND].update();
                plugin.getManager().cargaCarteles();     
                
                for (PlayerEffect pe : chetados) {
                    if (pe.getPl().equals(pl)) {
                        quitaEfectos(pe);
                        break;
                    }
                }
                
                Bukkit.broadcastMessage(EpicCastle.PLUGIN + Lang.CASTLE_RELEASED.getText().replace("%CASTLE%", "Castillo Diamante"));
            }
            if (money != null && money.compareToIgnoreCase(fac) == 0) {
                plugin.getManager().carteles[CASTLEMONEY].setLine(OWNER, "");
                plugin.getManager().carteles[CASTLEMONEY].update();
                plugin.getManager().cargaCarteles();    
                
                for (PlayerEffect pe : chetados) {
                    if (pe.getPl().equals(pl)) {
                        quitaEfectos(pe);
                        break;
                    }
                }
                
                Bukkit.broadcastMessage(EpicCastle.PLUGIN + Lang.CASTLE_RELEASED.getText().replace("%CASTLE%", "Castillo Dinero"));
            }
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
