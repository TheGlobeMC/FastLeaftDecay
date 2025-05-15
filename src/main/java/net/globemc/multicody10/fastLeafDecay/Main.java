package net.globemc.multicody10.fastLeafDecay;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * FastLeafDecay is a Folia-compatible plugin that causes leaf blocks to decay quickly
 * when a connected log is broken. It supports all vanilla overworld tree types and adds
 * particle/sound effects during decay.
 */
public final class Main extends JavaPlugin implements Listener {

    // Tree logs that trigger decay when broken
    private final Set<Material> logMaterials = Set.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
            Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG,
            Material.CHERRY_LOG, Material.BAMBOO_BLOCK
    );

    // Leaves that can decay
    private final Set<Material> leafMaterials = Set.of(
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES,
            Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.MANGROVE_LEAVES,
            Material.CHERRY_LEAVES, Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES
    );

    private static final int DECAY_DELAY_TICKS = 20;
    private static final int MAX_DECAY_BLOCKS = 128;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Listens for log blocks being broken and starts asynchronous decay logic.
     *
     * @param event The block break event.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!logMaterials.contains(block.getType())) return;

        // Start delayed decay using Folia's RegionScheduler
        getServer().getRegionScheduler().runDelayed(this, block.getLocation(), task -> decayLeavesAroundAsync(block), DECAY_DELAY_TICKS);
    }

    /**
     * Performs asynchronous leaf decay in a region-aware way using Folia's scheduler.
     * Spreads decay gradually over nearby leaves, giving a visual wave effect.
     *
     * @param startBlock The original log block that was broken.
     */
    private void decayLeavesAroundAsync(Block startBlock) {
        Set<Location> checked = new HashSet<>();
        Set<Block> toCheck = new HashSet<>();
        toCheck.add(startBlock);

        int decayDelay = 1;  // Delay in ticks between each leaf decay (tweak as necessary)

        // BFS-like search for leaves in the area
        while (!toCheck.isEmpty() && checked.size() < MAX_DECAY_BLOCKS) {
            Block current = toCheck.iterator().next();
            toCheck.remove(current);

            Location loc = current.getLocation();
            if (!checked.add(loc)) continue;

            // Only check if the current block is a leaf block and not connected to a log
            if (leafMaterials.contains(current.getType()) && !isLeafAttachedToLog(current)) {
                Material originalType = current.getType();
                BlockData originalData = current.getBlockData();
                Location currentLoc = current.getLocation();

                // Delay the decay of each individual leaf to give the "one by one" effect
                getServer().getRegionScheduler().runDelayed(this, currentLoc, task -> {
                    Block fresh = currentLoc.getBlock();
                    if (fresh.getType() == originalType) {
                        // Emit particle and sound at block center
                        Location effectLoc = currentLoc.clone().add(0.5, 0.5, 0.5);
                        fresh.breakNaturally(new ItemStack(Material.AIR));
                        fresh.getWorld().spawnParticle(Particle.FALLING_DUST, effectLoc, (int) randomNumber(1.0f, 10.0f), 0.2, 0.2, 0.2, 0.01, originalData);
                        fresh.getWorld().playSound(effectLoc, Sound.BLOCK_GRASS_BREAK, 1.0f, randomNumber(1.0f, 2.0f));
                        // fresh.setType(Material.AIR, false);
                    }
                }, decayDelay * checked.size());  // Delay by the number of leaves already checked

            }

            // Add adjacent blocks for checking (non-diagonal only)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) continue;

                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (leafMaterials.contains(neighbor.getType()) && !checked.contains(neighbor.getLocation())) {
                            toCheck.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the given leaf block is adjacent to any log block.
     *
     * @param leafBlock The leaf block to check.
     * @return true if attached to a log; false if isolated.
     */
    private boolean isLeafAttachedToLog(Block leafBlock) {
        // Check the neighboring blocks to see if any are logs
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) continue; // Only check adjacent blocks

                    Block neighbor = leafBlock.getRelative(dx, dy, dz);
                    if (logMaterials.contains(neighbor.getType())) {
                        return true; // Found a log block nearby, so the leaf is still attached
                    }
                }
            }
        }
        return false; // No log blocks found nearby, the leaf can decay
    }

    /**
     * Generates a random float between min (inclusive) and max (inclusive).
     *
     * @param min Minimum value.
     * @param max Maximum value.
     * @return A float between min and max.
     * @throws IllegalArgumentException if min > max.
     */
    public static float randomNumber(float min, float max) {
        final Random random = new Random();
        if (min > max) {
            throw new IllegalArgumentException("Min value cannot be greater than max value.");
        }
        return random.nextFloat((max - min) + 1) + min;
    }

}
