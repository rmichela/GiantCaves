//    Copyright (C) 2011  Ryan Michela
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.ryanmichela.giantcaves;

import java.util.Map;

/**
 */
public class Config {
    // Frequency
    public double fxz;
    public double fy;

    // Density
    public int cutoff;

    // Position
    public int caveBandMin;
    public int caveBandMax;

    // Debug
    public boolean debugMode;

    public Config(Map<String, Object> c) {
        if(c.containsKey("fxz")) {
            fxz = Double.parseDouble(c.get("fxz").toString());
        } else {
            fxz = 0.005;
        }

        if(c.containsKey("fy")) {
            fy = Double.parseDouble(c.get("fy").toString());
        } else {
            fy = 0.01;
        }

        if(c.containsKey("cutoff")) {
            cutoff = Integer.parseInt(c.get("cutoff").toString());
        } else {
            cutoff = 62;
        }

        if(c.containsKey("miny")) {
            caveBandMin = Integer.parseInt(c.get("miny").toString());
        } else {
            caveBandMin = 6;
        }

        if(c.containsKey("maxy")) {
            caveBandMax = Integer.parseInt(c.get("maxy").toString());
        } else {
            caveBandMax = 64;
        }

        if(c.containsKey("debug")) {
            debugMode = Boolean.parseBoolean(c.get("debug").toString());
        } else {
            debugMode = false;
        }
    }
}
