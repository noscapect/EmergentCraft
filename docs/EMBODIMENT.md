# Embodiment decision

## Decision: narrow custom-body prototype using tagged vanilla Villager

Fabric's `FakePlayer` is useful as an interaction proxy, but a `ServerPlayer` has lifecycle, connection, inventory synchronization, and navigation concerns that need version-specific integration testing before it can be declared a robust autonomous body. This environment has no JDK 25, so that API research cannot honestly be completed through a live 26.2 server run during this milestone.

The selected spike is a persistent, visible, disabled-AI vanilla `Villager` created through the server entity API. It has a stable registry link, ordinary health/death behavior, and uses Minecraft `Navigation.moveTo` for the one visible movement action. It is not repeatedly teleported. The disabled AI avoids vanilla villager goals becoming hidden inhabitant motivation.

## Limitations

This body does not yet provide player inventory, hunger, true player block breaking, crafting, or client player-list semantics. Perception is still minimal. The current local candidate is a short navigation attempt and may fail naturally because of terrain. It is a correct embodiment foundation, not a claim of full player equivalence.

## Next embodiment work

Run 26.2 integration tests for `FakePlayer` after JDK 25 installation. If it provides reliable visible state and player interactions, introduce it only as a constrained interaction proxy behind an `Embodiment` interface; retain a custom navigable body if FakePlayer navigation remains unsuitable. This hybrid avoids Microsoft-account bots and scales to arbitrary simulated inhabitants without credentials.

