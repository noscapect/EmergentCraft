# Architecture

`mod/` owns embodiment, local perception, candidate generation, action revalidation, and Minecraft execution. `brain/` owns provider selection, structured cognition, and durable memory. The only crossing is a loopback HTTP JSON request from the mod to `POST /v1/decide`.

The server tick builds an immutable compact snapshot, starts one `HttpClient.sendAsync` request per inhabitant, and returns immediately. Completion is scheduled back onto the Minecraft server thread. Before executing, the mod checks the agent epoch, life/pause state, embodiment, and exact offered action ID. Therefore late or invented decisions fail without modifying the world.

The milestone offers `WAIT` and a short `MOVE_TO` candidate. `MOVE_TO` invokes the body's Minecraft navigation; it is never implemented by long-distance teleportation. Candidate generation is intentionally an affordance layer, not a motivational policy.

The brain uses Ollama `/api/generate` structured JSON by default, or `EC_PROVIDER=mock` for deterministic testing. It binds its own HTTP server to `127.0.0.1`. No model output is evaluated as code, command, filesystem instruction, or direct world operation.

## Memory

`brain/data/<agent UUID>/episodes.jsonl` is append-only world/embodiment-created episodic fact data. `reflections.jsonl` contains model-authored subjective interpretations. `self.json` contains self-authored intent/goals. They are separate precisely so a reflection can never be treated as a fact. This is learning through persistent information, not weight training or a claim that model weights change.

`config/emergentcraft/agents.tsv` is the mod-side registry, outside world-save internals. It holds stable UUID, name, creation time, life/pause state, body UUID, and decision epoch.

