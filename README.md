# EmergentCraft — First Contact

An experimental Fabric mod and local cognition service for observing autonomous LLM inhabitants in an ordinary Minecraft survival world. Minecraft is the sole authority on world state. The brain can only select a currently offered legal action; it cannot issue Minecraft commands or alter the world.

## Status

The Brain vertical slice is complete and tested: it validates perception and action contracts, selects a candidate through Ollama or a deterministic provider, and persists separate episodic facts, subjective reflections, and self-authored state. The mod persists identities, creates visible goal-less but navigable Villager bodies, asks the local brain asynchronously, and can visibly walk a body to a nearby candidate position. It does not use teleportation for movement.

Java 25 is required for the mod. The included Windows launcher locates the registered JDK automatically, so it also works when an already-open terminal has not yet picked up the updated `PATH`.

## Run

The simplest Windows startup is double-clicking `start-emergentcraft.cmd` in the repository root. It finds the installed Java 25 JDK, starts the Brain and Fabric development server in separate windows, and installs Brain dependencies on first use.

To start components manually:

```powershell
# terminal 1
cd C:\CODE\EmergentCraft\brain
Copy-Item .env.example .env
npm install
npm run dev

# terminal 2, after JDK 25 is installed
cd C:\CODE\EmergentCraft\mod
.\gradlew.bat runServer
```

In the server console or as an operator in game:

```text
/ec spawn Rowan
/ec list
/ec inspect Rowan
/ec pause Rowan
/ec resume Rowan
/ec movetest Rowan
/ec remove Rowan
/ec removeorphan Rowan
/ec revive Rowan
/ec brainstatus
```

Use `/ec movetest Rowan` to prove native body movement before involving Ollama. It preempts any pending cognition or active EmergentCraft path, chooses a nearby reachable target, and reports path start/completion or failure. To confirm there is no hidden Villager wandering, use `/ec pause Rowan` and observe the idle body.

If an old world reports a loaded vanilla body after a chunk reload, `/ec repairbody Rowan` safely rebinds it. `/ec inspect Rowan` reports whether a body is controlled, awaiting restoration, unloaded, or missing.

`/ec remove <name>` refuses to delete an identity while its registered body chunk is unloaded, preventing a body from being left behind as an ordinary Villager. For recovery from a previous registry loss, it also removes every loaded, unregistered vanilla Villager whose custom name exactly equals `<name>`; for example `/ec remove Rowan` or `/ec remove Alice`. `/ec inspect <name>` identifies these as `ORPHANED_VANILLA`. Use `/ec removeorphan <name> <uuid>` only when you deliberately want to select one duplicate instead. Registered bodies with a different name are never touched.

`runServer` will create a development server. If you instead use a separately downloaded dedicated server, you must personally accept Mojang's EULA before running it; this repository never accepts it on your behalf.

Set `OLLAMA_MODEL` in `brain/.env`, or leave it blank to select the first installed model reported by Ollama. `npm run smoke:ollama` performs one schema-constrained request and prints no hidden reasoning.

See [architecture](docs/ARCHITECTURE.md), [experiment boundaries](docs/EXPERIMENT.md), and [embodiment decision](docs/EMBODIMENT.md).
