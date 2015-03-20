/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.jlh.epicCastle.utils;

import com.sk89q.worldedit.Vector;
import org.bukkit.Location;

/**
 *
 * @author Julian
 */
public class Area {
    
    private final Vector p1;
    private final Vector p2;
    private int area;

    public Area(Location loc, int area) {
        this.area = area;                
        
        p1 = new Vector(loc.getX() - (area/2), loc.getY() - (area/2), loc.getZ() - (area/2));
        p2 = new Vector(loc.getX() + (area/2), loc.getY() + (area/2), loc.getZ() + (area/2));
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }
    
    public boolean contains(Location p) {        
        if (p == null) return false;
        return new Vector(p.getX(), p.getY(), p.getZ()).containedWithin(p1, p2);    
    }
}
