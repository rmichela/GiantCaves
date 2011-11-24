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

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.*;

import java.util.Random;

/**
 */
public class GiantCavePopulator extends BlockPopulator{

    private Config config;

    public GiantCavePopulator(Config config)
    {
        this.config = config;
        blockingCoefficient = amplitude - config.cutoff;
        materialId = (byte)(config.debugMode ? 1 : 0); // Stone in debug, air in release
    }

    // Density
    private final int amplitude = 100;
    private final int blockingCoefficient;

    // Second pass
    private final double f2xz = 0.25;
    private final double f2y = 0.05;
    private final int amplitude2 = 2;

    // Position
    private final int caveBandBuffer = 16;

    // Material
    private final byte materialId;

    @Override
    public void populate(World world, Random random, Chunk source) {
        byte[] b = ((CraftChunk)source).getHandle().b;

        NoiseGenerator noiseGen = new SimplexNoiseGenerator(world);

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < 128; y++) {
                    if(y < config.caveBandMin || y > config.caveBandMax) continue;

                    double xx = (source.getX() << 4) | (x & 0xF);
                    double yy = y & 0x7F;
                    double zz = (source.getZ() << 4) | (z & 0xF);
                    if(    noiseGen.noise(xx * config.fxz, yy * config.fy, zz * config.fxz) * amplitude
                         + noiseGen.noise(xx * f2xz, yy * f2y, zz * f2xz) * amplitude2
                         - linearCutoffCoefficient(y) > config.cutoff)
                    {
                        b[blockOffset(x,z,y)] = materialId;
                    }
                }
            }
        }
    }

    private int blockOffset(int x, int z, int y) {
        return x << 11 | z << 7 | y;
    }

    private double linearCutoffCoefficient(int y) {
        // No distortion
        if( y < config.caveBandMin || y > config.caveBandMax) {
            return blockingCoefficient;
        // Bottom layer distortion
        } else if (y >= config.caveBandMin && y <= config.caveBandMin + caveBandBuffer) {
            int yy = y - config.caveBandMin;
            return (-blockingCoefficient / caveBandBuffer) * yy + blockingCoefficient;
        // Top layer distortion
        } else if (y <= config.caveBandMax && y >= config.caveBandMax - caveBandBuffer) {
            int yy = y - config.caveBandMax + caveBandBuffer;
            return (blockingCoefficient / caveBandBuffer) * yy;
        // Out of range
        } else {
            return 0;
        }
    }
}
