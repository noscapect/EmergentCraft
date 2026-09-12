# Continuity of Mind

Each stable agent UUID owns an independent mind directory under `brain/data/<agent-id>/`. There is no shared memory or telepathy.

## Layers

- `episodes.jsonl` is append-only Minecraft/embodiment factual history. Old line-only episodes remain readable as legacy records with unknown metadata.
- `knowledge.json` is evidence-derived world knowledge: encountered dimensions, biomes, entity/block/item types and people with first/last encounter metadata. It never assigns utility or current GPS positions.
- `reflections.jsonl`, `beliefs.json`, and `self.json` are model-authored subjective interpretation, self-model, and intent. They are never factual world records.
- `projects.json` holds at most three active self-authored projects. A missing decision update preserves them.
- `autobiography.jsonl` contains model-authored derived chapters; source episodes remain untouched.
- `memory-meta.json` tracks completed consolidation ranges.
- Skill starts, progress-relevant failures, hunger thresholds, eating, crafting, combat, placement, and build-plan outcomes are embodiment-authored factual episodes. Model intent about those events remains subjective.

## Recall and budget

Working events are recent and high fidelity. Older episodes are scored deterministically by current people/type/biome/location overlap, bounded active-project/self/belief retrieval cues, factual salience, and forward recency (newer otherwise-equal facts rank higher). Subjective cues only select stored factual records; they never create facts or knowledge. Relevant social and recalled facts are fitted before raw recent trivia. Autobiographical chapters are ranked by cue overlap and recency rather than simply loaded in append order. The default compact context budget is 6,000 serialized characters and includes every section, including self/projects/chapters; oversized sections are clipped or omitted before the limit is exceeded.

When factual events include authoritative Minecraft `worldTime`, recalled episode presentation uses simple Minecraft-relative labels such as `same Minecraft day` or `1 Minecraft day ago`. Legacy records retain wall-clock-relative labels.

The local `AsyncQueue` serves foreground cognition before queued background chapter consolidation. Consolidation starts after 30 un-consolidated events, summarizes batches of 20 through the configured provider, validates its small public output, and never deletes episodes. Failed summaries leave memory unchanged.

## Observer tools

`/ec mind <name>`, `/ec memories <name>`, and `/ec recall <name>` are diagnostics. The last produces a fresh current perception and asks only deterministic memory retrieval—never an Ollama decision or Minecraft action. Detailed output is written to the server log.

This architecture is memory-based learning and continuity, not fine-tuning, LoRA, embeddings, or a vector database.
