# Architecture

`mod/` owns embodiment, local perception, candidate generation, action revalidation, and Minecraft execution. `brain/` owns provider selection, structured cognition, and durable memory. The only crossing is a loopback HTTP JSON request from the mod to `POST /v1/decide`.

The server tick builds an immutable compact snapshot, starts one `HttpClient.sendAsync` request per inhabitant, and returns immediately. Completion is scheduled back onto the Minecraft server thread. Before executing, the mod checks the agent epoch, life/pause state, embodiment, and exact offered action ID. Therefore late or invented decisions fail without modifying the world.

Each inhabitant has runtime state: `IDLE`, `WAITING_FOR_COGNITION`, or `EXECUTING_ACTION`. New cognition is only requested from `IDLE`. A `MOVE_TO` keeps its action ID, start/target positions, tick timestamps, and last movement progress until native navigation reaches the arrival radius or fails by no path, path ending, stuck detection, timeout, or missing body.

## Agency

The brain selects a bounded, server-offered skill rather than a path node. The executor performs its necessary native navigation and validated primitives while runtime remains executing, then emits an authoritative factual outcome and returns to cognition. Current skills cover travel, acquire/break, pickup, eat, equip, engage, craft, place, and a bounded build plan. Skill code has no goals or survival policy.

Metabolism is per stable UUID and persists independently of body rewrapping. Hunger/saturation drain as body physics; food candidates are only actual carried items with Minecraft's `FOOD` component. A starving body takes Minecraft starvation damage, but no automatic eating, fleeing, tool choice, or target selection exists. Equipment persists by identity and is synchronized onto the visible body, so Minecraft's normal held-item combat and tool mechanics apply.

Crafting consults the loaded vanilla recipe manager and checks inventory and nearby crafting-table context before consuming ingredients and adding output. Placement uses an invisible Fabric `FakePlayer` only for vanilla player-context placement; the visible `GoalLessVillager` remains the body and navigator. Candidate generation round-robins neutral categories before its cap, preventing one long category from hiding all other legal affordances.

`/ec movetest` is an operator diagnostic, not an agent action. It forcefully cancels the current lifecycle state, stops a currently owned navigation path, clears runtime movement data, and enters a diagnostic execution state before creating its own native path. That invalidates the old pending decision epoch; a late LLM response can therefore never replace the diagnostic action. Diagnostic outcomes are logged but are not added to the agent's episodic memory.

`perception/PerceptionBuilder` constructs `schemaVersion: 1` records and Gson serializes them. It reads actual body health/state/effects, persistent agent hunger/saturation/equipment, dimension/weather/time/light/biome, bounded local entities/items, exposed LOS-visible blocks, recent server-owned factual events, and a bounded carried inventory. Entity/item visibility uses a 14-block local radius and LOS; blocks use a 7-block radius and LOS. Sampling is capped and deterministically rotated by stable agent ID plus epoch, avoiding resource-type ordering bias.

Candidates are opaque server IDs and retain a server-side `OfferedAction` with exact entity UUID, block position/type, or item UUID/type. The available physical affordances are WAIT, native MOVE_TO/APPROACH, timed and range-checked BREAK_BLOCK, range/capacity-checked PICK_UP_ITEM, and local SPEAK. Every target is checked again when selected and on completion. Navigation always uses Minecraft `PathNavigation`; block destruction uses `Level.destroyBlock` only after native approach, range, hardness, and elapsed-time validation. No action teleports or accepts a model coordinate.

`AgentInventoryStore` is a 36-slot/64-per-stack authoritative carried inventory keyed by stable agent UUID, persisted outside the world. It is therefore preserved when a vanilla-saved body is rewrapped. There are no starting items. FakePlayer is isolated to a local placement proxy; it never replaces the visible body or native navigation.

The brain uses one server-wide Ollama `/api/generate` structured-JSON configuration by default, or `EC_PROVIDER=mock` for deterministic testing. Provider/model/profile are selected in `brain/.env`; all residents share inference capacity while receiving only their own memory context. It binds its own HTTP server to `127.0.0.1`. No model output is evaluated as code, command, filesystem instruction, or direct world operation.

## Memory

`brain/data/<agent UUID>/episodes.jsonl` is append-only world/embodiment-created episodic fact data. The mod reports ACTION, OUTCOME, DAMAGE, DEATH, SPEECH, and ACQUISITION through loopback `POST /v1/event`; the LLM cannot create these events. Count-only observation spam is not written. `reflections.jsonl` contains model-authored subjective interpretations. `self.json` contains self-authored intent/goals. The prompt labels SELF, CURRENT WORLD PERCEPTION, recent facts, memories, subjective state/reflections, and AVAILABLE ACTION CANDIDATES separately.

The Brain owns a FIFO global `AsyncQueue`; `OLLAMA_MAX_CONCURRENT` defaults to one. Queue/start/completion timing and selected public action are logged, while HTTP requests wait asynchronously and never block a server tick.

## Continuity of mind

The Brain now retains per-agent working memory, immutable factual episodes, objective known-world records, subjective reflections/beliefs/self-model, active projects, social evidence, and model-authored autobiographical chapters. Retrieval is deterministic and bounded: currently perceived people/types/biome/location, project language, factual salience, and recency score candidate episodes. It is not a shared global memory, vector database, or model-weight training system. `/v1/recall`, `/v1/mind`, and `/v1/memories` are loopback observer endpoints used by operator diagnostics only.

`config/emergentcraft/agents.tsv` is the mod-side registry, outside world-save internals. It holds stable UUID, name, creation time, life/pause state, body UUID, and decision epoch.
