# Agency

The model decides **what** to attempt from a compact set of legal candidates. The deterministic executor decides only **how** to carry out that already-selected skill with Minecraft primitives. It never supplies a goal, target, recipe, hunger policy, building template or survival rule.

`IDLE -> WAITING_FOR_COGNITION -> EXECUTING_ACTION -> IDLE` prevents cognition while a healthy native path or multi-placement plan is in progress. Failure (missing target/item, blocked path, changed block, unavailable ingredients, invalid placement) stops safely, emits factual outcome text, and returns eligibility to cognition.

Metabolism is UUID-local and persistent: hunger is 0-20 plus saturation; it drains gradually and starvation damages the visible body. Food uses the item `FOOD` component and is offered only when carried. No automatic eating exists.

Craft candidates are derived from the active vanilla recipe manager and current inventory. Crafting validates ingredients/output capacity and requires a nearby table for table-sized recipes. Placement requires carried block items, a local offered air position and support. The only FakePlayer use is an invisible placement proxy demanded by vanilla's player placement context.

Build plans are model-authored local relative placements, capped at 24 and each validated against owned block items and world state. They are not house templates. Candidate categories are round-robin sampled so travel, entities, resources, inventory, crafting, placement and social action are represented fairly.
