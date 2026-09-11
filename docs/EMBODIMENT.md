# Embodiment decision

## Decision: goal-less navigable Villager subclass

Fabric's `FakePlayer` is useful as an interaction proxy, but a `ServerPlayer` has lifecycle, connection, inventory synchronization, and navigation concerns that need version-specific integration testing before it can be declared a robust autonomous body. The 26.2 custom-body implementation now compiles against Java 25 and Fabric Loom 1.14.10; FakePlayer still needs a dedicated live-server interaction investigation before it can be chosen responsibly.

The first live test found that `Villager.setNoAi(true)` prevents the native server AI step from ticking both `PathNavigation` and movement controls; a started path therefore never becomes walking.

`GoalLessVillager` is created directly with the vanilla `minecraft:villager` entity type, so ordinary clients still render it as a Villager. It calls `removeFreeWill()` to remove registered goals and Brain behaviours, leaves `NoAI` **false**, and overrides `customServerAiStep` with a no-op. Minecraft `Mob.serverAiStep` still performs native navigation, move-control, look-control, collision, terrain, and animation work, while `Villager.customServerAiStep` cannot tick villager schedules, POI behaviours, social routines, wandering, or other vanilla motivation.

Movement is exclusively started by the EmergentCraft executor through `Navigation.moveTo`. No movement path is implemented by repeated coordinate changes or teleporting. Because a subclass constructed with the vanilla type is saved by Minecraft as a normal Villager, the mod replaces only registered embodiment UUIDs with a fresh `GoalLessVillager` on server start; this is embodiment restoration, not movement.

## Limitations

This body does not yet provide player inventory, hunger, true player block breaking, crafting, or client player-list semantics. Perception is still minimal. The current local candidate is a short navigation attempt and may fail naturally because of terrain. `/ec movetest <name>` searches several nearby 3–5 block destinations, accepts only a reachable native path, and reports start/reached/failure without involving Ollama.

An idle, paused inhabitant has no EmergentCraft navigation path and no Villager Brain/goal behaviour, so it should not wander. Verify this in each live test with `/ec pause <name>` before trusting a movement result.

## Next embodiment work

Run 26.2 integration tests for `FakePlayer` after JDK 25 installation. If it provides reliable visible state and player interactions, introduce it only as a constrained interaction proxy behind an `Embodiment` interface; retain a custom navigable body if FakePlayer navigation remains unsuitable. This hybrid avoids Microsoft-account bots and scales to arbitrary simulated inhabitants without credentials.
