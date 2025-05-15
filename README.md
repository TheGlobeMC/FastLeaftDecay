# 🌿 FastLeafDecay (Folia Edition)

A high-performance, Folia-optimized plugin that accelerates natural leaf decay when tree logs are broken. Now with dynamic particles, nature-inspired sound design, and wave-like staggered decay for a deeply immersive and visually polished experience. Designed for large-scale SMP servers where both aesthetics and performance matter.

Licensed under the MIT License – see the [LICENSE](https://github.com/TheGlobeMC/FastLeaftDecay/blob/master/LICENSE) file for details.

---

## 🔧 Features

- ⚡ **Ultra Lightweight**: Asynchronously decays up to 128 disconnected leaf blocks per tree break without affecting TPS.
- 🌍 **Folia Native**: Built from the ground up using Folia’s `RegionScheduler` for safe, thread-aware task scheduling.
- 🍃 **Intelligent Decay Logic**: Scans surrounding leaves and decays only those no longer connected to logs.
- ✨ **Immersive Effects**:
    - `FALLING_DUST` particles for natural crumble visuals.
    - Occasional `CLOUD` and `VILLAGER_HAPPY` particles for subtle ambience.
    - Thread-safe sound effects like `BLOCK_GRASS_BREAK` and ambient pitch-bent buzzes.
- 🌳 **Multi-Tree Support**: Compatible with all overworld foliage types including Azalea, Cherry, Bamboo, and Flowering leaves.
- 🌀 **Wave-Style Decay Animation**: Leaf blocks decay in a natural, expanding wave thanks to a progressive and bounded delay system.
- 🎛️ **Optimized for Speed & Style**: Uses compact BFS for leaf/log connection detection with bounded radius and path caching.

---

## 🚀 How It Works

1. A player breaks a tree log or leaf.
2. The plugin schedules a lightweight task using Folia's `RegionScheduler`.
3. A recursive decay scan begins, identifying non-attached leaves within a 3-block Chebyshev radius.
4. Each leaf block is decayed with a short delay staggered to produce a ripple effect.
5. Particle and sound effects are emitted for each leaf decay, with rare ambient extras.

---

## ✅ Supported Versions

- **Minecraft**: 1.20+
- **Server**: [Folia](https://github.com/PaperMC/Folia)

> ❌ Not compatible with Spigot or standard Paper. Requires Folia’s region-based async scheduler.

---

## 🧠 Technical Notes

- Up to **128 leaf blocks** processed per trigger to avoid runaway recursion.
- **Delay staggering** capped with a `MAX_DELAY` to prevent long latency trees.
- Custom BFS logic uses a compact coordinate hashing algorithm for fast set lookup.
- **Visual feedback** controlled via tick-based delays and rare particle conditions.
- All operations are **region-thread-safe** via Folia's strict scheduling context.

---