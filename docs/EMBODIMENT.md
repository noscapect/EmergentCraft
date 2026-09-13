# Embodiment decision

## Decision: goal-less navigable Villager subclass

Fabric's `FakePlayer` is useful as an interaction proxy, but a `ServerPlayer` has lifecycle, connection, inventory synchronization, and navigation concerns that need version-specific integration testing before it can be declared a robust autonomous body. The 26.2 custom-body implementation now compiles against Java 25 and Fabric Loom 1.14.10; FakePlayer still needs a dedicated live-server interaction investigation before it can be chosen responsibly.

The first live test found that `Villager.setNoAi(true)` prevents the native server AI step from ticking both `PathNavigation` and movement controls; a started path therefore never becomes walking.

`GoalLessVillager` is created directly with the vanilla `minecraft:villager` entity type, so ordinary clients still render it as a Villager. It calls `removeFreeWill()` to remove registered goals and Brain behaviours, leaves `NoAI` **false**, and overrides `customServerAiStep` with a no-op. Minecraft `Mob.serverAiStep` still performs native navigation, move-control, look-control, collision, terrain, and animation work, while `Villager.customServerAiStep` cannot tick villager schedules, POI behaviours, social routines, wandering, or other vanilla motivation.

Movement is exclusively started by the EmergentCraft executor through `Navigation.moveTo`. No movement path is implemented by repeated coordinate changes or teleporting. Because a subclass constructed with the vanilla type is saved by Minecraft as a normal Villager, the mod replaces only registered embodiment UUIDs with a fresh `GoalLessVillager` on server start; this is embodiment restoration, not movement.

After free will is removed, the body reinstalls only vanilla `FloatGoal` and `PathNavigation.setCanFloat(true)`. This is water-safe locomotion capability, not a motivation: the Villager Brain remains suppressed and no wandering, survival, or target-selection goal is restored.

Server-start repair alone is insufficient: an entity can deserialize as vanilla when its chunk loads later. The mod also subscribes to Fabric 26.2 `ServerEntityEvents.ENTITY_LOAD`. It compares the loaded entity UUID with the agent registry, schedules a guarded server-thread rewrap only for that exact registered vanilla Villager, atomically rebinds the registry to the replacement UUID after it was added successfully, then discards the old body. It preserves position, yaw, pitch, custom name, and current health. Unrelated Villagers never match and are untouched.

## Limitations

The body now has persistent agent hunger/saturation physics, selected equipment synchronized to body slots, and recipe/food skill support. It remains a Villager, not a player. Fabric's `FakePlayer` is used only as an invisible local interaction proxy because vanilla block placement and player melee require a player context. It never navigates, receives cognition, or replaces the visible body.

This body does not provide a vanilla player food bar or client player-list semantics, but does provide persistent agent hunger/saturation, equipment, food skills and recipe skills. `/ec movetest <name>` searches several nearby 3–5 block destinations, accepts only a reachable native path, and reports start/reached/failure without involving Ollama.

Eyes and Hands adds a bounded `AgentInventoryStore` rather than relying on Villager's non-public trading inventory: it is authoritative for agent pickup, has 36 item kinds at up to 64 each, starts empty, and persists against the stable agent UUID so body rewrapping preserves it. Fabric 26.2 `FakePlayer` is limited to an invisible, snapped-to-body vanilla interaction context for block placement and a single accepted melee attack; it is never an agent body, navigator, or inventory authority. Combat mirrors only the authoritative selected main-hand item into the proxy, revalidates the exact offered live target and melee range, and records the target health delta. The bounded inventory store cannot represent item durability/component mutation, so proxy-side weapon durability is intentionally not synchronized back to it.

BREAK_BLOCK approaches using the visible Villager's native navigation, requires the originally offered block type to remain nearby and breakable, waits a hardness-derived bounded number of ticks, then invokes Minecraft's normal `Level.destroyBlock(..., drops=true, body, ...)`. It does not mine remotely or bypass bedrock. PICK_UP_ITEM similarly requires the exact offered `ItemEntity`, range, and inventory capacity before moving its real stack into the agent inventory and discarding the world entity.

An idle, paused inhabitant has no EmergentCraft navigation path and no Villager Brain/goal behaviour, so it should not wander. Verify this in each live test with `/ec pause <name>` before trusting a movement result.

`/ec movetest <name>` intentionally preempts an in-flight cognition request or ordinary movement action. `/ec inspect <name>` exposes this as `runtime=EXECUTING_ACTION (movetest)`. Once the diagnostic path succeeds or fails, runtime returns to `IDLE` and normal cognition becomes eligible again.

## Chunk lifecycle diagnostics

`/ec inspect <name>` is safe even if no body is loaded. It reports `CONTROLLED`, `VANILLA_NEEDS_RESTORE`, `UNLOADED`, `MISSING`, `INVALID_REGISTRY`, `WRONG_ENTITY`, or `DEAD`, together with the registered body UUID, loaded class/UUID when available, runtime state, and active move.

`/ec repairbody <name>` repairs a currently loaded vanilla registered body immediately. It reports an unloaded chunk instead of forcing it to load. If a body is genuinely missing or the registry lacks an embodiment ID, the explicit command recreates a controlled body near the invoking operator while retaining the agent's stable cognitive UUID/name/memory identity.

`/ec remove <name>` never removes a registry record while its known body is unloaded: the operator must load the chunk first, so the entity and its identity are removed together. A historical registry loss can leave vanilla Villagers with no associated identity. In that exceptional case, `/ec inspect <name>` reports `ORPHANED_VANILLA`; `/ec remove <name>` removes every loaded, unregistered vanilla Villager with that exact custom name. This is generic for every agent name, not special-cased to Rowan. `/ec removeorphan <name> <uuid>` remains available when an operator deliberately wants to choose just one duplicate. It will not touch registered bodies with a different name.

### Manual chunk-reload test

1. Run `/ec spawn Rowan` and `/ec movetest Rowan`; observe normal walking.
2. Disconnect and allow Rowan's chunk to unload, then reconnect and load the area again.
3. Run `/ec inspect Rowan`; it must report `body=CONTROLLED` after the entity-load repair.
4. Run `/ec movetest Rowan` again and observe walking.
5. Restart the server and repeat steps 3–4. The same agent UUID/name must remain in the registry.

## Next embodiment work

Run 26.2 integration tests for `FakePlayer` after JDK 25 installation. If it provides reliable visible state and player interactions, introduce it only as a constrained interaction proxy behind an `Embodiment` interface; retain a custom navigable body if FakePlayer navigation remains unsuitable. This hybrid avoids Microsoft-account bots and scales to arbitrary simulated inhabitants without credentials.
