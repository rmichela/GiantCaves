package com.ryanmichela.giantcaves;

import net.minecraft.server.Material;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.*;

import java.util.Random;

/**
 */
public class GiantCavePopulator extends BlockPopulator{

    // Frequency
    private static final double fxz = 0.005;
    private static final double fy = 0.01;

    // Density
    private static final int cutoff = 62;
    private static final int amplitude = 100;
    private static final int blockingCoefficient = amplitude - cutoff;

    // Second pass
    private static final double f2xz = 0.25;
    private static final double f2y = 0.05;
    private static final int amplitude2 = 2;

    // Position
    private static final int caveBandMin = 6;
    private static final int caveBandMax = 74;
    private static final int caveBandBuffer = 16;

    // Material
    private static final int materialId = 0;

    @Override
    public void populate(World world, Random random, Chunk source) {
        byte[] b = ((CraftChunk)source).getHandle().b;

        NoiseGenerator noiseGen = new SimplexNoiseGenerator(world);

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < 128; y++) {
                    if(y < caveBandMin || y > caveBandMax) continue;

                    double xx = (source.getX() << 4) | (x & 0xF);
                    double yy = y & 0x7F;
                    double zz = (source.getZ() << 4) | (z & 0xF);
                    if(    noiseGen.noise(xx * fxz, yy * fy, zz * fxz) * amplitude
                         + noiseGen.noise(xx * f2xz, yy * f2y, zz * f2xz) * amplitude2
                         - linearCutoffCoefficient(y) > cutoff)
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
        if( y < caveBandMin || y > caveBandMax) {
            return blockingCoefficient;
        // Bottom layer distortion
        } else if (y >= caveBandMin && y <= caveBandMin + caveBandBuffer) {
            int yy = y - caveBandMin;
            return (-blockingCoefficient / caveBandBuffer) * yy + blockingCoefficient;
        // Top layer distortion
        } else if (y <= caveBandMax && y >= caveBandMax - caveBandBuffer) {
            int yy = y - caveBandMax + caveBandBuffer;
            return (blockingCoefficient / caveBandBuffer) * yy;
        // Out of range
        } else {
            return 0;
        }
    }
}
