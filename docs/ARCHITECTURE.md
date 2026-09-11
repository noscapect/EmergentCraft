# Architecture

`mod/` owns embodiment, local perception, candidate generation, action revalidation, and Minecraft execution. `brain/` owns provider selection, structured cognition, and durable memory. The only crossing is a loopback HTTP JSON request from the mod to `POST /v1/decide`.

The server tick builds an immutable compact snapshot, starts one `HttpClient.sendAsync` request per inhabitant, and returns immediately. Completion is scheduled back onto the Minecraft server thread. Before executing, the mod checks the agent epoch, life/pause state, embodiment, and exact offered action ID. Therefore late or invented decisions fail without modifying the world.

Each inhabitant has runtime state: `IDLE`, `WAITING_FOR_COGNITION`, or `EXECUTING_ACTION`. New cognition is only requested from `IDLE`. A `MOVE_TO` keeps its action ID, start/target positions, tick timestamps, and last movement progress until native navigation reaches the arrival radius or fails by no path, path ending, stuck detection, timeout, or missing body.

`/ec movetest` is an operator diagnostic, not an agent action. It forcefully cancels the current lifecycle state, stops a currently owned navigation path, clears runtime movement data, and enters a diagnostic execution state before creating its own native path. That invalidates the old pending decision epoch; a late LLM response can therefore never replace the diagnostic action. Diagnostic outcomes are logged but are not added to the agent's episodic memory.

The milestone offers `WAIT` and a short `MOVE_TO` candidate. `MOVE_TO` invokes the body's Minecraft navigation; it is never implemented by long-distance teleportation. Candidate generation is intentionally an affordance layer, not a motivational policy.

The brain uses Ollama `/api/generate` structured JSON by default, or `EC_PROVIDER=mock` for deterministic testing. It binds its own HTTP server to `127.0.0.1`. No model output is evaluated as code, command, filesystem instruction, or direct world operation.

## Memory

`brain/data/<agent UUID>/episodes.jsonl` is append-only world/embodiment-created episodic fact data. The mod reports physical `ACTION` and `OUTCOME` facts through loopback `POST /v1/event`; the LLM cannot create these events. `reflections.jsonl` contains model-authored subjective interpretations. `self.json` contains self-authored intent/goals. The next cognition context includes bounded episodic facts, reflections, and self state, while labelling those categories distinctly. This is learning through persistent information, not weight training or a claim that model weights change.

`config/emergentcraft/agents.tsv` is the mod-side registry, outside world-save internals. It holds stable UUID, name, creation time, life/pause state, body UUID, and decision epoch.
