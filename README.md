# 🌿 FastLeafDecay (Folia Edition)
A high-performance, Folia-optimized plugin that accelerates natural leaf decay when tree logs are broken, complete with particle and sound effects for enhanced immersion. Designed for large-scale SMP servers where visual polish and performance matter.

## 🔧 Features
- ⚡ **Fast and Lightweight:** Decays nearby leaf blocks after a tree log is broken with zero TPS impact.
- 🌍 **Folia Native:** Uses `RegionScheduler` for safe, asynchronous scheduling.
- 🍃 **Smart Detection:** Only decays leaves no longer connected to logs.
- ✨ **Immersive Effects:** Adds `FALLING_DUST` particles and grass break sounds during decay.
- 🌳 **Multi-Tree Support:** Handles all overworld trees including Azalea, Cherry, and Bamboo.

## 🚀 How It Works
1. Player breaks a log block.
2. Plugin waits 1 second (20 ticks) and begins checking surrounding leaves.
3. Leaves not connected to any logs are decayed one by one with effects.
4. Decay is region-safe, non-blocking, and visually smooth.

## ✅ Supported Versions

- Minecraft: `1.20+`
- Server: [Folia](https://github.com/PaperMC/Folia) (async fork of Paper)

> ❌ Not compatible with vanilla Paper or Spigot. Requires Folia's `RegionScheduler`.

## 🧠 Technical Notes

- Maximum of 128 leaf blocks are processed per tree break.
- Leaf decay delays are staggered by tick count to create a wave-like animation.
- Particle and sound emissions are region-aware and thread-safe.
