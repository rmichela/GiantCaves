package com.ryanmichela.giantcaves;

import org.bukkit.Chunk;
import org.bukkit.util.noise.NoiseGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

/**
 * Copyright 2013 Ryan Michela
 */
public class GCRandom {
    public Chunk chunk;
    private Config config;

    // Note: Smaller frequencies yield slower change (more stretched out)
    //   Larger amplitudes yield greater influence on final void
    // Frequency
    private final double f1xz;
    private final double f1y;
    // Density
    private final int amplitude1 = 100;
    private final double subtractForLessThanCutoff;
    // Second pass - small noise
    private final double f2xz = 0.25;
    private final double f2y = 0.05;
    private final int amplitude2 = 2;
    // Third pass - vertical noise
    private final double f3xz = 0.025;
    private final double f3y = 0.005;
    private final int amplitude3 = 20;
    // Position
    private final int caveBandBuffer;

    // Noise
    private final NoiseGenerator noiseGen1;
    private final NoiseGenerator noiseGen2;
    private final NoiseGenerator noiseGen3;

    public GCRandom(Chunk chunk, Config config) {
        this.chunk = chunk;
        this.config = config;
        subtractForLessThanCutoff = amplitude1 - config.cutoff;
        f1xz = 1.0 / config.sxz;
        f1y = 1.0 / config.sy;
        if (config.caveBandMax - config.caveBandMin > 128) {
            caveBandBuffer = 32;
        } else {
            caveBandBuffer = 16;
        }
        noiseGen1 = new SimplexNoiseGenerator(chunk.getWorld());
        noiseGen2 = new SimplexNoiseGenerator((long) noiseGen1.noise(chunk.getX(), chunk.getZ()));
        noiseGen3 = new SimplexNoiseGenerator((long) noiseGen1.noise(chunk.getX(), chunk.getZ()));
    }

    public boolean isInGiantCave(int x, int y, int z) {
        double xx = (chunk.getX() << 4) | (x & 0xF);
        double yy = y;
        double zz = (chunk.getZ() << 4) | (z & 0xF);

        double n1 = (noiseGen1.noise(xx * f1xz, yy * f1y, zz * f1xz) * amplitude1);
        double n2 = (noiseGen2.noise(xx * f2xz, yy * f2y, zz * f2xz) * amplitude2);
        double n3 = (noiseGen3.noise(xx * f3xz, yy * f3y, zz * f3xz) * amplitude3);
        double lc = linearCutoffCoefficient(y);

        boolean isInCave = n1 + n2 - n3 - lc > config.cutoff;
        return isInCave;
    }

    private double linearCutoffCoefficient(int y) {
        // Out of bounds
        if (y < config.caveBandMin || y > config.caveBandMax) {
            return subtractForLessThanCutoff;
            // Bottom layer distortion
        } else if (y >= config.caveBandMin && y <= config.caveBandMin + caveBandBuffer) {
            double yy = y - config.caveBandMin;
            return (-subtractForLessThanCutoff / (double) caveBandBuffer) * yy + subtractForLessThanCutoff;
            // Top layer distortion
        } else if (y <= config.caveBandMax && y >= config.caveBandMax - caveBandBuffer) {
            double yy = y - config.caveBandMax + caveBandBuffer;
            return (subtractForLessThanCutoff / (double) caveBandBuffer) * yy;
            // In bounds, no distortion
        } else {
            return 0;
        }
    }
}
