# EmergentCraft project memory

- Milestone: **Eyes and Hands**.
- `mod/` is Fabric for Minecraft Java 26.2 using official Mojang mappings, Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, Loom 1.14.10, and Java 25. `start-emergentcraft.cmd` discovers the registered JDK and starts both components.
- `brain/` is Node/TypeScript. `npm run test`, `npm run build`, `npm run smoke:ollama` run from that folder.
- The mod calls `POST http://127.0.0.1:3847/v1/decide` asynchronously. Never block a Minecraft server tick on HTTP.
- World truth is constructed in the mod. The brain only receives a compact snapshot and candidate IDs; it cannot invent target IDs or commands.
- Episodic facts are appended by the embodiment. Reflections and goals are model-authored subjective state and remain separate.
- Do not add survival, social, building, or progression scoring/rules. Affordances are not goals.
- Current body is a `GoalLessVillager`: a vanilla-type Villager subclass with `NoAI=false`, cleared free will, and a suppressed Villager Brain tick. Minecraft's native navigation/control ticks remain active. No normal movement teleportation. See `docs/EMBODIMENT.md` before changing it.
- Runtime action state is `IDLE`, `WAITING_FOR_COGNITION`, or `EXECUTING_ACTION`; never start cognition while an action executes. Minecraft reports factual action outcomes to `POST /v1/event`.
- Perception contract is `schemaVersion: 1`, built in `mod/.../perception/` and serialized with Gson. Never fabricate unsupported body state: this Villager has no player hunger; `capabilities.playerHunger=false`.
- Perception is local and bounded: entity/item radius 14 with LOS, block radius 7 with LOS, and neutral deterministic ordering based on stable agent ID and decision epoch. Candidate IDs and targets are generated and revalidated only by the mod.
- Agent inventory is a bounded 36-slot, 64-per-stack `AgentInventoryStore`, persisted by stable agent UUID outside the world, so rewrapping a Villager body retains possessions. No starting items or unlimited inventory.
- Physical actions currently include WAIT, native MOVE/APPROACH, timed range-checked BREAK_BLOCK, range/capacity-checked PICK_UP_ITEM, and local SPEAK. FakePlayer is not used: it has not been needed for this narrow executor and must not replace proven Villager navigation without separate verification.
- The Brain has a FIFO global Ollama queue (`OLLAMA_MAX_CONCURRENT=1` default). It must never block Minecraft ticks.
- Vanilla-type bodies rehydrate as ordinary Villagers after chunk load. `ServerEntityEvents.ENTITY_LOAD` rewraps only registry-matched embodiment UUIDs; use `/ec inspect` and `/ec repairbody` for diagnostics/recovery.
- Runtime memory (`brain/data`, `mod/run`, worlds, logs) is ignored.
