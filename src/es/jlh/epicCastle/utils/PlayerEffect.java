/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jlh.epicCastle.utils;

import org.bukkit.entity.Player;

/**
 *
 * @author Julian
 */
public class PlayerEffect {
    
    private Player pl;
    
    private boolean vel;
    private boolean salto;
    private boolean fuerza;
    private boolean reg;

    public PlayerEffect(Player pl, boolean vel, boolean salto, boolean fuerza, boolean reg) {
        this.pl = pl;
        this.vel = vel;
        this.salto = salto;
        this.fuerza = fuerza;
        this.reg = reg;
    }

    public Player getPl() {
        return pl;
    }

    public void setPl(Player pl) {
        this.pl = pl;
    }

    public boolean isVel() {
        return vel;
    }

    public void setVel(boolean vel) {
        this.vel = vel;
    }

    public boolean isSalto() {
        return salto;
    }

    public void setSalto(boolean salto) {
        this.salto = salto;
    }

    public boolean isFuerza() {
        return fuerza;
    }

    public void setFuerza(boolean fuerza) {
        this.fuerza = fuerza;
    }

    public boolean isReg() {
        return reg;
    }

    public void setReg(boolean reg) {
        this.reg = reg;
    }    
}
