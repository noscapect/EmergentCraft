# EmergentCraft — First Contact

An experimental Fabric mod and local cognition service for observing autonomous LLM inhabitants in an ordinary Minecraft survival world. Minecraft is the sole authority on world state. The brain can only select a currently offered legal action; it cannot issue Minecraft commands or alter the world.

## Status

The Brain vertical slice is complete and tested: it validates perception and action contracts, selects a candidate through Ollama or a deterministic provider, and persists separate episodic facts, subjective reflections, and self-authored state. The mod is a deliberately small 26.2 Fabric embodiment spike: it persists identities, creates visible disabled-AI villager bodies, asks the local brain asynchronously, and can visibly walk a body to a nearby candidate position. It does not use teleportation for movement.

Java 25 is required for the mod. This machine did not have `java` on PATH when this milestone was assembled, so mod compilation/runtime must be performed after installing a user-scoped JDK 25 (for example Temurin 25) and reopening the terminal.

## Run

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
/ec remove Rowan
/ec revive Rowan
/ec brainstatus
```

`runServer` will create a development server. If you instead use a separately downloaded dedicated server, you must personally accept Mojang's EULA before running it; this repository never accepts it on your behalf.

Set `OLLAMA_MODEL` in `brain/.env`, or leave it blank to select the first installed model reported by Ollama. `npm run smoke:ollama` performs one schema-constrained request and prints no hidden reasoning.

See [architecture](docs/ARCHITECTURE.md), [experiment boundaries](docs/EXPERIMENT.md), and [embodiment decision](docs/EMBODIMENT.md).
