# Architecture

`mod/` owns embodiment, local perception, candidate generation, action revalidation, and Minecraft execution. `brain/` owns provider selection, structured cognition, and durable memory. The only crossing is a loopback HTTP JSON request from the mod to `POST /v1/decide`.

The server tick builds an immutable compact snapshot, starts one `HttpClient.sendAsync` request per inhabitant, and returns immediately. Completion is scheduled back onto the Minecraft server thread. Before executing, the mod checks the agent epoch, life/pause state, embodiment, and exact offered action ID. Therefore late or invented decisions fail without modifying the world.

Each inhabitant has runtime state: `IDLE`, `WAITING_FOR_COGNITION`, or `EXECUTING_ACTION`. New cognition is only requested from `IDLE`. A `MOVE_TO` keeps its action ID, start/target positions, tick timestamps, and last movement progress until native navigation reaches the arrival radius or fails by no path, path ending, stuck detection, timeout, or missing body.

`/ec movetest` is an operator diagnostic, not an agent action. It forcefully cancels the current lifecycle state, stops a currently owned navigation path, clears runtime movement data, and enters a diagnostic execution state before creating its own native path. That invalidates the old pending decision epoch; a late LLM response can therefore never replace the diagnostic action. Diagnostic outcomes are logged but are not added to the agent's episodic memory.

`perception/PerceptionBuilder` constructs `schemaVersion: 1` records and Gson serializes them. It reads actual body health/state/effects, dimension/weather/time/light/biome, bounded local entities/items, exposed LOS-visible blocks, recent server-owned factual events, and a bounded carried inventory. The Villager has no player hunger capability, so hunger is absent rather than fabricated. Entity/item visibility uses a 14-block local radius and LOS; blocks use a 7-block radius and LOS. Sampling is capped and deterministically rotated by stable agent ID plus epoch, avoiding resource-type ordering bias.

Candidates are opaque server IDs and retain a server-side `OfferedAction` with exact entity UUID, block position/type, or item UUID/type. The available physical affordances are WAIT, native MOVE_TO/APPROACH, timed and range-checked BREAK_BLOCK, range/capacity-checked PICK_UP_ITEM, and local SPEAK. Every target is checked again when selected and on completion. Navigation always uses Minecraft `PathNavigation`; block destruction uses `Level.destroyBlock` only after native approach, range, hardness, and elapsed-time validation. No action teleports or accepts a model coordinate.

`AgentInventoryStore` is a 36-slot/64-per-stack authoritative carried inventory keyed by stable agent UUID, persisted outside the world. It is therefore preserved when a vanilla-saved body is rewrapped. There are no starting items. FakePlayer is deliberately not introduced: the bounded current actions work with the visible body and adding a player proxy without focused 26.2 validation would broaden embodiment risk.

The brain uses Ollama `/api/generate` structured JSON by default, or `EC_PROVIDER=mock` for deterministic testing. It binds its own HTTP server to `127.0.0.1`. No model output is evaluated as code, command, filesystem instruction, or direct world operation.

## Memory

`brain/data/<agent UUID>/episodes.jsonl` is append-only world/embodiment-created episodic fact data. The mod reports ACTION, OUTCOME, DAMAGE, DEATH, SPEECH, and ACQUISITION through loopback `POST /v1/event`; the LLM cannot create these events. Count-only observation spam is not written. `reflections.jsonl` contains model-authored subjective interpretations. `self.json` contains self-authored intent/goals. The prompt labels SELF, CURRENT WORLD PERCEPTION, recent facts, memories, subjective state/reflections, and AVAILABLE ACTION CANDIDATES separately.

The Brain owns a FIFO global `AsyncQueue`; `OLLAMA_MAX_CONCURRENT` defaults to one. Queue/start/completion timing and selected public action are logged, while HTTP requests wait asynchronously and never block a server tick.

`config/emergentcraft/agents.tsv` is the mod-side registry, outside world-save internals. It holds stable UUID, name, creation time, life/pause state, body UUID, and decision epoch.
