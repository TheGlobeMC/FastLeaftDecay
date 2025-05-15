package net.globemc.multicody10.fastLeafDecay;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin implements Listener {

    private static final Set<Material> LOG_MATERIALS = Set.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
            Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG,
            Material.CHERRY_LOG, Material.BAMBOO_BLOCK
    );

    private static final Set<Material> LEAF_MATERIALS = Set.of(
            Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES,
            Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.MANGROVE_LEAVES,
            Material.CHERRY_LEAVES, Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES
    );

    private static final int MAX_DECAY_BLOCKS = 128;
    private static final int BASE_DELAY_TICKS = 1;
    private static final int LEAF_LOG_CHECK_RADIUS = 3;

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        if (LOG_MATERIALS.contains(type) || LEAF_MATERIALS.contains(type)) {
            scheduleDecayCheck(block.getLocation());
        }
    }

    private void scheduleDecayCheck(Location loc) {
        getServer().getRegionScheduler().runDelayed(this, loc, task -> decayLeavesAround(loc), BASE_DELAY_TICKS);
    }

    private void decayLeavesAround(Location startLoc) {
        World world = startLoc.getWorld();
        if (world == null) return;

        Set<Long> checked = new HashSet<>();
        Deque<Block> toCheck = new ArrayDeque<>();
        toCheck.add(world.getBlockAt(startLoc));

        int delayAccumulator = 0;

        while (!toCheck.isEmpty() && checked.size() < MAX_DECAY_BLOCKS) {
            Block current = toCheck.pollFirst();
            long currentKey = toLongHash(current.getX(), current.getY(), current.getZ());

            if (!checked.add(currentKey)) continue;

            if (LEAF_MATERIALS.contains(current.getType()) && !isLeafAttachedToLog(current)) {
                Material originalType = current.getType();
                BlockData originalData = current.getBlockData();

                int scheduledDelay = delayAccumulator;
                delayAccumulator += BASE_DELAY_TICKS;

                Location loc = current.getLocation();
                getServer().getRegionScheduler().runDelayed(this, loc, task -> {
                    Block blockAtLoc = loc.getBlock();
                    if (blockAtLoc.getType() == originalType) {
                        blockAtLoc.breakNaturally(new ItemStack(Material.AIR));

                        Location effectLoc = loc.clone().add(0.5, 0.5, 0.5);
                        blockAtLoc.getWorld().spawnParticle(Particle.FALLING_DUST, effectLoc, 6 + random.nextInt(6), 0.2, 0.2, 0.2, 0.01, originalData);
                        blockAtLoc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, effectLoc, 2, 0.1, 0.1, 0.1, 0.01);
                        blockAtLoc.getWorld().spawnParticle(Particle.CRIT, effectLoc, 2, 0.05, 0.05, 0.05, 0.01);
                        blockAtLoc.getWorld().spawnParticle(Particle.END_ROD, effectLoc, 1, 0.0, 0.0, 0.0, 0.0);
                        // Occasional ambient wisp (cloud, rare)
                        if (random.nextInt(15) == 0) {
                            blockAtLoc.getWorld().spawnParticle(Particle.CLOUD, effectLoc, 1, 0.05, 0.05, 0.05, 0.0);
                        }

                        // Nature trail shimmer (green sparks, low chance)
                        if (random.nextInt(7) == 0) {
                            blockAtLoc.getWorld().spawnParticle(Particle.COMPOSTER, effectLoc, 1, 0.02, 0.02, 0.02, 0.01);
                        }

                        // BEE_LOOP is subtle audio that evokes a pollinator ambience, low chance.
                        if (random.nextInt(10) == 0) {
                            blockAtLoc.getWorld().playSound(effectLoc, Sound.ENTITY_BEE_LOOP, 0.15f, 1.9f + random.nextFloat() * 0.1f);
                        }
                        blockAtLoc.getWorld().playSound(effectLoc, Sound.BLOCK_GRASS_BREAK, 0.8f, 1.1f + random.nextFloat() * 0.2f);
                        blockAtLoc.getWorld().playSound(effectLoc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 0.3f, 2.0f + random.nextFloat());
                        blockAtLoc.getWorld().playSound(effectLoc, Sound.BLOCK_HANGING_ROOTS_BREAK, 0.4f, 1.8f + random.nextFloat() * 0.3f);
                    }
                }, Math.max(1, scheduledDelay));
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue;

                        Block neighbor = current.getRelative(dx, dy, dz);
                        long neighborKey = toLongHash(neighbor.getX(), neighbor.getY(), neighbor.getZ());
                        if (LEAF_MATERIALS.contains(neighbor.getType()) && !checked.contains(neighborKey)) {
                            toCheck.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    // Returns a unique long key for 3D coords (X,Y,Z), shifts to avoid collision
    private static long toLongHash(int x, int y, int z) {
        return (((long) x) & 0x3FFFFFL) << 42 | (((long) y) & 0x3FFFFFL) << 21 | (((long) z) & 0x1FFFFFL);
    }

    private boolean isLeafAttachedToLog(Block leaf) {
        World world = leaf.getWorld();
        int startX = leaf.getX();
        int startY = leaf.getY();
        int startZ = leaf.getZ();

        Deque<int[]> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        queue.add(new int[]{startX, startY, startZ});
        visited.add(toLongHash(startX, startY, startZ));

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0], cy = current[1], cz = current[2];

            // Distance squared using Chebyshev distance for performance, radius 4
            int dx = Math.abs(cx - startX);
            int dy = Math.abs(cy - startY);
            int dz = Math.abs(cz - startZ);
            if (dx > LEAF_LOG_CHECK_RADIUS || dy > LEAF_LOG_CHECK_RADIUS || dz > LEAF_LOG_CHECK_RADIUS) continue;

            Block block = world.getBlockAt(cx, cy, cz);
            Material type = block.getType();

            if (LOG_MATERIALS.contains(type)) return true;

            // Only traverse air or leaves
            if (!LEAF_MATERIALS.contains(type) && !type.isAir()) continue;

            for (int[] offset : OFFSETS) {
                int nx = cx + offset[0], ny = cy + offset[1], nz = cz + offset[2];
                long hash = toLongHash(nx, ny, nz);
                if (!visited.contains(hash)) {
                    visited.add(hash);
                    queue.add(new int[]{nx, ny, nz});
                }
            }
        }
        return false;
    }

    private static final int[][] OFFSETS = {
            {1, 0, 0}, {-1, 0, 0},
            {0, 1, 0}, {0, -1, 0},
            {0, 0, 1}, {0, 0, -1}
    };
}