# Agency

The model decides **what** to attempt from a compact set of legal candidates. The deterministic executor decides only **how** to carry out that already-selected skill with Minecraft primitives. It never supplies a goal, target, recipe, hunger policy, building template or survival rule.

`IDLE -> WAITING_FOR_COGNITION -> EXECUTING_ACTION -> IDLE` prevents cognition while a healthy native path or multi-placement plan is in progress. Failure (missing target/item, blocked path, changed block, unavailable ingredients, invalid placement) stops safely, emits factual outcome text, and returns eligibility to cognition.

Metabolism is UUID-local and persistent: hunger is 0-20 plus saturation; it drains gradually and starvation damages the visible body. Food uses the item `FOOD` component and is offered only when carried. No automatic eating exists.

Craft candidates are derived from the active vanilla recipe manager and current inventory. Crafting validates ingredients/output capacity and requires a nearby table for table-sized recipes. Placement requires carried block items, a local offered air position and support. The only FakePlayer use is an invisible placement proxy demanded by vanilla's player placement context.

Build plans are model-authored local relative placements, capped at 24 and each validated against owned block items and world state. They are not house templates. Candidate categories are round-robin sampled so travel, entities, resources, inventory, crafting, placement and social action are represented fairly.

Approach is not an empty loop: targets already within interaction distance are not offered as APPROACH candidates. Nearby perceived players and other EmergentCraft residents receive an explicit SPEAK affordance; speech remains a model-selected, factual event rather than a social script.

Physical search is a bounded capability, not world knowledge. Its native-navigation legs expand from the present position, use only loaded dry standable endpoints, and inspect a requested type only through ordinary local LOS perception after arrival. A no-path, ended path, stuck leg, or timeout advances silently; one final found/not-found outcome is recorded. An exhausted target/origin pair is briefly suppressed in recent experience and becomes eligible again after meaningful movement.
