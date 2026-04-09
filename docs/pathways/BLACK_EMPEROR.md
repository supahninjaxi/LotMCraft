# Black Emperor Pathway Abilities

## Active Abilities

---

### Entropy *(sub-ability of Disorder)*
**Sequence Requirement:** 2 *(accessible via Disorder Mode 6)*
**Duration:** 60 seconds (1200 ticks)
**Pulse Interval:** Every 400 ticks (3 pulses total)
**Aura Radius:** 25 blocks

**Scale Multiplier:** `1.0 + (2 − seq) × 0.25` (stronger at lower sequences)
**Spirituality Drain per Pulse:** `0.75 + (stack × 0.35) + (seq_gap × 0.20)`

**Pulse Outcomes (roll-based, scale increases with stack):**

| Roll | Outcome | Effect |
|------|---------|--------|
| < 25 | Minor Entropy | Slowness + Blindness + Confusion |
| 25–49 | Control Collapse | Losing Control (6 + stack levels) + Unluck (3 + stack levels) |
| 50–69 | Sensory Decay | Blindness + Confusion + Slowness III |
| 70–85 | Entropy Drain | Blindness + Confusion + Slowness III + Losing Control |
| 86–95 | Entropy Damage | 6% max HP + (stack × 1.25) direct damage |
| 96+ | Annihilation | 12% max HP + (stack × 2.5) direct damage + Losing Control + Unluck |

---

### Commanding Orders
**Sequence Requirement:** 3
**Spirituality Cost:** 3.0 per tick (toggle)
*(Cannot be used by NPCs)*

- **Range:** 25 blocks
- **Chat Command Cooldown:** 2 seconds between uses
- While active, sending a chat message in the format `<TargetName> <command>` issues an order:

| Command | Effects |
|---------|---------|
| `kneel` | Slowness II (50t), Weakness (40t), Confusion (25t), velocity reduced to 20% |
| `halt` | Slowness III (60t), zeroed velocity, mobs get No-AI for 60t |
| `retreat` | Slowness (40t), pushed away at 0.65 force with 0.12 upward |
| `advance` | Slowness (25t), mobs navigate to caster at 1.0 speed, players pulled at 0.35 force |
| `silence` | Weakness (50t), Confusion (35t), all Beyonder abilities disabled for 280 ticks (14 seconds) |

**Head Lock:** Targets 2+ sequences weaker have their head locked downward (36–54° based on gap).

**Damage Reflection:** When the caster takes damage from a Beyonder under presence, applies Weakness II + Slowness III to the attacker and pushes them away.

---

### Commanding Presence
**Sequence Requirement:** 3
**Spirituality Cost:** 3.0 per tick (toggle)
*(Cannot be used by NPCs)*

- Emits a passive aura affecting all nearby entities weaker than the caster.
- **Aura Radius:** 22 blocks × scale (scale = `1.0 + (3 − seq) × 0.20`)

**Aura Effects (Seq 3 and below):**
- **Confusion:** Level 0, 30 ticks — applied every 40 ticks
- **Reverence messages** sent to players every 100 ticks
- Head-down pressure on targets 2+ sequences weaker

**Legacy Version (Seq 5):**
- **Slowness:** Level 0, 50 ticks — applied every 40 ticks to players
- **Weakness:** Level 0, 50 ticks — applied every 40 ticks to Beyonders of equal or lower sequence

**Combat Spirituality Drain (Seq 5 and below):** Targets in combat with the caster lose `1.0 + (seq_gap × 0.25)` spirituality per tick.

**Damage Reflection:** Same as Commanding Orders — Weakness II + Slowness III to attacker, knockback applied.

---

### Frenzy *(sub-ability of Disorder)*
**Sequence Requirement:** 3 *(accessible via Disorder Mode 5)*
**Cooldown:** 60 seconds
**Duration:** 60 seconds (1200 ticks)
**Pulse Interval:** Every 400 ticks (3 pulses total)
**Aura Radius:** 18 blocks

**Caster pulses** (random outcome each pulse):
- **Boon (70–88% chance):** Random buff — Speed II, Jump Boost II, Strength II, Luck II, Absorption II, or Regeneration II + minor heal
- **Neutral (92–98%):** Damage Resistance or Slow Falling
- **Minor Bad (2–6%):** Blindness + Confusion
- **Major Bad (0.2%):** Unluck II + Losing Control

**Target pulses** (outcome varies by sequence gap):
| Outcome | Effect |
|---------|--------|
| Minor Disorder | Slowness (20–80t) + Blindness (40t) + Confusion (40–80t) |
| Control Loss | Losing Control / Confusion / Weakness for 60–140t + Unluck |
| Seal | Slowness III + Weakness + ability seal for 80–140t |
| Lightning Sweep | 4–8 lightning strikes in 12-block radius |
| Tornado Burst | Summons a tornado at the target |
| Meteor Strike | Summons a meteor at the target |

---

### Bestowment
**Sequence Requirement:** 4
**Spirituality Cost:** 45
**Cooldown:** 4.0 seconds
*(Cannot be copied or replicated)*

- **Target Range:** 18 blocks
- **Resistance:** ~40% at 1-sequence gap, 0% at 2+ gap

Five selectable modes. Duration of each effect scales with caster sequence (stronger = longer):

**Mode 0 — Money Focus**
- **Duration:** 160–320 ticks (scales with sequence)
- Pulls the target toward the nearest ore within 192 blocks (iron, gold, diamond).
- Mobs navigate to ore at 1.0 speed; players are lightly pulled every 5 ticks.
- Message: *"Money... ore... treasure..."*

**Mode 1 — Rash**
- **Duration:** 160–300 ticks (scales with sequence)
- Applies **Confusion** (30t) + **Slowness II** (20t) every 8 ticks.
- Causes chaotic movement nudges every tick.
- Triggers a random ability cast every 8 ticks.
- Pulses damage to the nearest entity within 12 blocks every 8 ticks.

**Mode 2 — Sluggish**
- **Duration:** 200–360 ticks (scales with sequence)
- Applies **Slowness II** (40t) every 20 ticks.
- Drains spirituality every 20 ticks.
- Blocks all ability usage for the duration.

**Mode 3 — Anxiety**
- **Duration:** 140–200 ticks (scales with sequence)
- Applies **Losing Control** (Level 3 at Seq 4+) + Confusion (40t) + Weakness (40t) every 20 ticks.
- Causes natural sanity loss.

**Mode 4 — Will to Fight Seal**
- **Duration:** 120–320 ticks (scales with sequence)
- Applies **Weakness II** + **Slowness II** for the full duration.
- Seals all Beyonder abilities until the effect expires.

---

### Exploit
**Sequence Requirement:** 4
**Spirituality Cost:** 15
**Cooldown:** 4.0 seconds
*(Cannot be copied or replicated; players only)*

Three selectable modes:

**Mode 0 — Flight** *(toggle)*
- Enables sustained flight. Drains 0.03–0.08 spirituality per tick.
- **Flight Speed by Sequence:**
  - Seq 2: 0.12
  - Seq 3: 0.10
  - Seq 4: 0.085
- Deactivates when spirituality drops below 0.5.

**Mode 1 — Jump Up** *(vertical launch)*
- Launches the caster straight upward (Y velocity +10.5, horizontal retained at 20%).
- Additional +3.5 Y boost for 3 ticks.
- **Fall damage immunity:** 120 ticks.

**Mode 2 — Jump Forward**
- Launches the caster forward (look × 10.0 + 25% current horizontal, Y +8.0).
- Additional look-direction boost for 3 ticks.
- **Fall damage immunity:** 120 ticks.

---

### Magnify
**Sequence Requirement:** 4
**Spirituality Cost:** 60
**Cooldown:** 20.0 seconds
*(Cannot be copied or replicated)*

- **Target Range:** 96 blocks
- **Resistance:** ~35% at 1-sequence gap, ~20% at 2-sequence gap

Four selectable modes:

**Mode 0 — Magnify Self** *(self-cast)*
- **Duration:** 240 ticks (12 seconds)
- Grants: Speed IV, Jump Boost IV, Strength VI, Damage Resistance IV, Regeneration III, Absorption IV, Haste III.

**Mode 1 — Magnify Weather**
- **Raining/thundering:** Triggers a weakened Lightning Storm at the target location.
- **Clear weather:** Spawns a Tornado at the target location.

**Mode 2 — Magnify Grab**
- **Duration:** 80 ticks (4 seconds)
- Pulls the target toward the caster at variable speed (1.1–2.6 based on distance).
- On arrival (within 2.1 blocks): Slowness VII (30t) + Weakness II (30t) + Blindness (30t) + Confusion (30t).

**Mode 3 — Magnify Execution**
- Deals damage scaled by sequence gap (`target_seq − caster_seq`):

| Sequence Gap | Damage |
|-------------|--------|
| +2 or more (weaker) | target current HP + 20 (execution) |
| +1 (slightly weaker) | 65% of target max HP |
| 0 (equal) | 50% of target max HP |
| −1 (slightly stronger) | 25% of target max HP |
| −2 or more (stronger) | 0% (no damage) |

---

### Disorder
**Sequence Requirement:** 5
**Spirituality Cost:** 65
**Cooldown:** 7.0 seconds
*(Cannot be copied or replicated)*

**Resistance:** ~30% at 1-sequence gap, 0% at 2+ gap

Seven selectable modes:

**Mode 0 — Disordered Actions**
- The target's next outgoing hit is redirected to a random nearby entity within 8 blocks (not the attacker or original target).
- **Duration:** 120 ticks (6 seconds)

**Mode 1 — Disordered Perception**
- 40% chance per outgoing hit to deal 0 damage.
- Applies **Confusion** (140t, Level 0).
- **Duration:** 140 ticks (7 seconds)

**Mode 2 — Defensive Veil** *(self-cast)*
- Grants the caster a **35% chance to negate any incoming hit**.
- **Duration:** 160 ticks (8 seconds)

**Mode 3 — Break Bonds** *(no target required)*
- Cleanses all harmful effects and fire from the chosen entity (self or other).
- Players receive full saturation + hunger restoration.

**Mode 4 — Distance Warp** *(self-cast)*
- Teleports the caster forward up to **8–34 blocks** (scales with sequence: `min(34, 8 + (8 − seq) × 3)`).
- Checks for a safe landing position block-by-block.

**Mode 5 — Frenzy** *(Seq 3+ only)*
- See **Frenzy** sub-ability above.

**Mode 6 — Entropy** *(Seq 2+ only)*
- See **Entropy** sub-ability above.

---

### Corrosion
**Sequence Requirement:** 6
**Spirituality Cost:** 2.0 per tick (toggle)
*(Cannot be used by NPCs)*

- **Aura Radius:** 10 blocks
- Entities in range accumulate exposure ticks. Stronger Beyonders (lower sequence than caster) are fully immune.

**Corruption Stages (by exposure ticks):**

| Stage | Threshold | Effects (every 40–60 ticks) | Message |
|-------|-----------|----------------------------|---------|
| 1 | 60+ ticks | Slowness I; mobs randomly halt navigation (35% chance) | "You feel an inexplicable greed stirring..." |
| 2 | 160+ ticks | Confusion; mobs randomly retarget nearby entities (50% chance) | "The greed is overwhelming — your thoughts are scattered." |
| 3 | 300+ ticks | Weakness II; Beyonder targets fire random abilities (60% chance every 60t) | "You can no longer control yourself. Darkness consumes you." |

**Exposure Decay:** Decays at half rate when leaving the aura.

---

### Weakness Detection
**Sequence Requirement:** 6
**Spirituality Cost:** 2.5 per tick (toggle)
*(Cannot be used by NPCs)*

- **Detection Range:** 20 blocks
- **Debuff Range:** 12 blocks
- Scans nearby entities and assigns violation tiers visually. Stronger Beyonders (lower sequence) are ignored.

**Violation conditions** (each adds +1 tier):
- Armor below 10
- Health below 30% of max
- Speed above 0.22

**Tier Effects (applied within 12 blocks, every 20 ticks):**

| Tier | Color | Damage Boost | Debuffs |
|------|-------|-------------|---------|
| 1 | Yellow | +15% | Slowness I, 40t |
| 2 | Orange | +30% | Slowness II (40t) + Weakness I (40t) |
| 3 | Red | +50% | Slowness III (40t) + Weakness II (40t) + 5 direct damage every 20t |

---

### Distortion
**Sequence Requirement:** 6
**Spirituality Cost:** 55
**Cooldown:** 6.0 seconds
*(Cannot be copied or replicated)*

**Target Range:** 12 blocks (14 blocks for Distort Intent at Seq 5+)
**Resistance:** ~35% at 1-sequence gap, 0% at 2+ gap

Five selectable modes:

**Mode 0 — Distort Action**
- Negates the target's next outgoing hit (0 damage).
- **Duration:** 160 ticks (Seq 5+) / 120 ticks (Seq 6+)
- **Seq 5+:** Also applies Weakness (60t) + Slowness (40t).

**Mode 1 — Distort Intent**
- Mobs lose target and navigation; retarget a random nearby entity.
- Players are randomly nudged every 4–5 ticks at 0.18–0.24 force.
- **Duration:** 100 ticks (Seq 5+) / 80 ticks (Seq 6+)
- **Seq 5+:** Also applies Confusion (60t) + Weakness (60t); radius extends to 14 blocks.

**Mode 2 — Distort Trajectory**
- Each projectile the target fires is rotated by 60–120° randomly.
- **Duration:** 160 ticks (Seq 5+) / 120 ticks (Seq 6+)
- **Charges:** 2 (Seq 5+) / 1 (Seq 6+)

**Mode 3 — Distort Concept** *(Seq 5+ only)*
- Links caster and target for 160 ticks.
- All damage dealt by either party is split 50/50 between both.

**Mode 4 — Distort Wound** *(Seq 4+ only)*
- Self-cast. Heals the caster for **35% of missing HP**.

---

### Briber
**Sequence Requirement:** 7
**Spirituality Cost:** 75
**Cooldown:** 8.5 seconds
*(Cannot be copied or replicated)*

- **Target Range:** 18 blocks
- **Requirement:** Must hold an item in the off-hand (transferred to target on cast).
- **Resistance:** ~50% at 1-sequence gap, 0% at 2+ gap

Three selectable modes, all lasting **100 ticks**:

**Mode 0 — Weaken**
- **Slowness II** (100t) + **Weakness** (100t).
- Target's armor reduced by 20%; outgoing damage reduced by 20%.

**Mode 1 — Arrogance**
- **Confusion** (40t) every 20 ticks.
- Mobs lose target and navigation; random movement nudges every 4 ticks.
- **20% chance per incoming hit** to completely dodge the damage.
- Triggers a random ability cast every 20 ticks.

**Mode 2 — Charm**
- **Slowness III** (20t) every 10 ticks.
- Mobs lose target and navigation; random movement nudges every 4 ticks.
- Charmed target deals **0 damage** to the caster.

---

### Eloquence
**Sequence Requirement:** 9
**Spirituality Cost:** 5
**Cooldown:** 5.5 seconds

- **Radius:** 5 blocks
- Applies to all nearby entities (excluding caster):
  - **Weakness:** Level 1, 10 seconds
- Caster receives **Hero of the Village** (Level 0, 10 seconds).

---

### Law Proficiency
**Sequence Requirement:** 9 *(superseded by Weakness Detection at Sequence 6 and below)*
**Spirituality Cost:** 1.5 per tick (toggle)

**Detection Radius:** 6 blocks

**Violation conditions** (each adds +1 tier):
- Armor below 10
- Health below 30% of max
- Speed above 0.25

**On violation detected:**
- **Weakness:** Level 1, 40 ticks
- **Slowness:** Level 0, 40 ticks
- Outgoing damage against the violator is boosted by **1.5×** within 6 blocks

**Note:** Beyonders of higher sequence (Seq < 9) are ignored entirely.

---

## Passive Abilities

---

### Physical Enhancements (Black Emperor)
**Sequence Requirement:** 9

Passive buffs that scale with the caster's current sequence.

| Sequence | Strength | Speed | Regeneration | Bonus Health | Resistance |
|----------|----------|-------|--------------|--------------|------------|
| 9        | —        | +1    | +1           | —            | —          |
| 8        | +2       | +2    | +2           | +7           | +5         |
| 7        | +2       | +2    | +2           | +6           | +5         |
| 6        | +3       | +2    | +2           | +8           | +6         |
| 5        | +3       | +2    | +2           | +10          | +8         |
| 4        | +3       | +4    | +3           | +16          | +13        |
| 3        | +4       | +4    | +3           | +18          | +5         |
| 2        | +5       | +4    | +4           | +24          | +6         |
| 1        | +4       | +4    | +4           | +30          | +18        |
| 0        | +6       | +5    | +5           | +36          | +8         |
