# Embodiment decision

## Decision: goal-less navigable Villager subclass

Fabric's `FakePlayer` is useful as an interaction proxy, but a `ServerPlayer` has lifecycle, connection, inventory synchronization, and navigation concerns that need version-specific integration testing before it can be declared a robust autonomous body. The 26.2 custom-body implementation now compiles against Java 25 and Fabric Loom 1.14.10; FakePlayer still needs a dedicated live-server interaction investigation before it can be chosen responsibly.

The first live test found that `Villager.setNoAi(true)` prevents the native server AI step from ticking both `PathNavigation` and movement controls; a started path therefore never becomes walking.

`GoalLessVillager` is created directly with the vanilla `minecraft:villager` entity type, so ordinary clients still render it as a Villager. It calls `removeFreeWill()` to remove registered goals and Brain behaviours, leaves `NoAI` **false**, and overrides `customServerAiStep` with a no-op. Minecraft `Mob.serverAiStep` still performs native navigation, move-control, look-control, collision, terrain, and animation work, while `Villager.customServerAiStep` cannot tick villager schedules, POI behaviours, social routines, wandering, or other vanilla motivation.

Movement is exclusively started by the EmergentCraft executor through `Navigation.moveTo`. No movement path is implemented by repeated coordinate changes or teleporting. Because a subclass constructed with the vanilla type is saved by Minecraft as a normal Villager, the mod replaces only registered embodiment UUIDs with a fresh `GoalLessVillager` on server start; this is embodiment restoration, not movement.

Server-start repair alone is insufficient: an entity can deserialize as vanilla when its chunk loads later. The mod also subscribes to Fabric 26.2 `ServerEntityEvents.ENTITY_LOAD`. It compares the loaded entity UUID with the agent registry, schedules a guarded server-thread rewrap only for that exact registered vanilla Villager, atomically rebinds the registry to the replacement UUID after it was added successfully, then discards the old body. It preserves position, yaw, pitch, custom name, and current health. Unrelated Villagers never match and are untouched.

## Limitations

This body does not yet provide player inventory, hunger, true player block breaking, crafting, or client player-list semantics. Perception is still minimal. The current local candidate is a short navigation attempt and may fail naturally because of terrain. `/ec movetest <name>` searches several nearby 3–5 block destinations, accepts only a reachable native path, and reports start/reached/failure without involving Ollama.

An idle, paused inhabitant has no EmergentCraft navigation path and no Villager Brain/goal behaviour, so it should not wander. Verify this in each live test with `/ec pause <name>` before trusting a movement result.

`/ec movetest <name>` intentionally preempts an in-flight cognition request or ordinary movement action. `/ec inspect <name>` exposes this as `runtime=EXECUTING_ACTION (movetest)`. Once the diagnostic path succeeds or fails, runtime returns to `IDLE` and normal cognition becomes eligible again.

## Chunk lifecycle diagnostics

`/ec inspect <name>` is safe even if no body is loaded. It reports `CONTROLLED`, `VANILLA_NEEDS_RESTORE`, `UNLOADED`, `MISSING`, `INVALID_REGISTRY`, `WRONG_ENTITY`, or `DEAD`, together with the registered body UUID, loaded class/UUID when available, runtime state, and active move.

`/ec repairbody <name>` repairs a currently loaded vanilla registered body immediately. It reports an unloaded chunk instead of forcing it to load. If a body is genuinely missing or the registry lacks an embodiment ID, the explicit command recreates a controlled body near the invoking operator while retaining the agent's stable cognitive UUID/name/memory identity.

`/ec remove <name>` never removes a registry record while its known body is unloaded: the operator must load the chunk first, so the entity and its identity are removed together. A historical registry loss can leave a vanilla Villager with no associated identity. In that exceptional case, `/ec inspect <name>` reports `ORPHANED_VANILLA`, and `/ec removeorphan <name>` removes exactly one loaded, unregistered vanilla Villager with that exact custom name. It refuses zero or multiple matches, and will not touch a registered or controlled body.

### Manual chunk-reload test

1. Run `/ec spawn Rowan` and `/ec movetest Rowan`; observe normal walking.
2. Disconnect and allow Rowan's chunk to unload, then reconnect and load the area again.
3. Run `/ec inspect Rowan`; it must report `body=CONTROLLED` after the entity-load repair.
4. Run `/ec movetest Rowan` again and observe walking.
5. Restart the server and repeat steps 3–4. The same agent UUID/name must remain in the registry.

## Next embodiment work

Run 26.2 integration tests for `FakePlayer` after JDK 25 installation. If it provides reliable visible state and player interactions, introduce it only as a constrained interaction proxy behind an `Embodiment` interface; retain a custom navigable body if FakePlayer navigation remains unsuitable. This hybrid avoids Microsoft-account bots and scales to arbitrary simulated inhabitants without credentials.
