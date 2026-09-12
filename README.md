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
/ec activity Rowan
/ec perceive Rowan
/ec actions Rowan
/ec mind Rowan
/ec memories Rowan
/ec recall Rowan
/ec pause Rowan
/ec resume Rowan
/ec movetest Rowan
/ec remove Rowan
/ec removeorphan Rowan
/ec revive Rowan
/ec brainstatus
```

Use `/ec movetest Rowan` to prove native body movement before involving Ollama. It preempts any pending cognition or active EmergentCraft path, chooses a nearby reachable target, and reports path start/completion or failure. To confirm there is no hidden Villager wandering, use `/ec pause Rowan` and observe the idle body.

`/ec perceive Rowan` prints real local health, biome, visible entity/block/item counts, and legal action count; `/ec perceive Rowan full` writes the bounded structured view to the server log. `/ec actions Rowan` logs the offered candidates without asking Ollama. The body observes nearby line-of-sight entities/items (14 blocks) and exposed blocks (7 blocks), then may choose legal WAIT, movement/approach, block-break, item-pickup, or local speech actions. These are affordances, never priorities.

The current body has a persistent agent metabolism (hunger/saturation) visible to cognition, while its carried inventory remains an explicit bounded 36-slot agent inventory persisted by stable identity through body rewrapping. `OLLAMA_MAX_CONCURRENT=1` limits global local-model concurrency by default; requests queue asynchronously while Minecraft keeps ticking.

`/ec mind Rowan` shows stored self/project/belief and memory counts. `/ec memories Rowan` writes recent factual episodes and autobiographical chapters to the server log. `/ec recall Rowan` builds current perception and logs the exact bounded long-term context that would be supplied to Rowan, without asking for a decision. `/ec activity Rowan` reports activity, persistent hunger/saturation, selected equipment and inventory. Skills are capabilities, never orders: the model can choose legal travel, acquire, eat, equip, engage, craft, placement, or a bounded self-authored build plan. See [memory architecture](docs/MEMORY.md) and [agency](docs/AGENCY.md).

If an old world reports a loaded vanilla body after a chunk reload, `/ec repairbody Rowan` safely rebinds it. `/ec inspect Rowan` reports whether a body is controlled, awaiting restoration, unloaded, or missing.

`/ec remove <name>` refuses to delete an identity while its registered body chunk is unloaded, preventing a body from being left behind as an ordinary Villager. For recovery from a previous registry loss, it also removes every loaded, unregistered vanilla Villager whose custom name exactly equals `<name>`; for example `/ec remove Rowan` or `/ec remove Alice`. `/ec inspect <name>` identifies these as `ORPHANED_VANILLA`. Use `/ec removeorphan <name> <uuid>` only when you deliberately want to select one duplicate instead. Registered bodies with a different name are never touched.

`runServer` will create a development server. If you instead use a separately downloaded dedicated server, you must personally accept Mojang's EULA before running it; this repository never accepts it on your behalf.

Set the one server-wide model in `brain/.env`. The default is `sweaterdog/andy-4` with profile `minecraft-andy4`; startup verifies it through Ollama and never silently chooses another model. All inhabitants share this inference engine but retain strictly separate UUID-keyed memories. `npm run smoke:ollama` performs one schema-constrained request and prints no hidden reasoning.

See [architecture](docs/ARCHITECTURE.md), [experiment boundaries](docs/EXPERIMENT.md), and [embodiment decision](docs/EMBODIMENT.md).

### Qwen via Ollama

Set `EC_PROVIDER=ollama`, `EC_MODEL_PROFILE=qwen`, and `OLLAMA_MODEL=<installed Qwen tag>` in `brain/.env`, then restart the Brain. This changes the model for the whole server, never one resident.

### Andy-4 via Ollama

Run `ollama pull sweaterdog/andy-4`, then use `EC_PROVIDER=ollama`, `EC_MODEL_PROFILE=minecraft-andy4`, and `OLLAMA_MODEL=sweaterdog/andy-4` in `brain/.env`. EmergentCraft never downloads model weights automatically.

### Andy-4.1 via LM Studio / llama-server

Start an OpenAI-compatible server only on `127.0.0.1:1234`, then set `EC_PROVIDER=openai-compatible`, `LOCAL_OPENAI_BASE_URL=http://127.0.0.1:1234/v1`, `LOCAL_OPENAI_MODEL=<loaded-model-id>`, and `EC_MODEL_PROFILE=minecraft-andy41`; restart the Brain. See [model details](docs/MODELS.md).
