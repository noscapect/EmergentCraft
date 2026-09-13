# Experiment boundaries

EmergentCraft asks what happens when inhabitants can perceive, remember, decide, and physically attempt actions in an ordinary survival world without a supplied high-level mission. Minecraft provides consequences; it decides whether an action succeeded.

This milestone does **not** prescribe survival, gathering, building, friendships, farming, social scores, resource targets, or a happiness/reward function. It also does not grant items, construct buildings, fake dialogue, teleport for navigation, or respawn a dead incarnation automatically.

The available actions are deliberately limited by engineering maturity, not used as desired outcomes. A model may wait indefinitely, make poor choices, or eventually die; such results are valid observation. The first reliable loop matters more than a theatrical village.

Agency expands the body, not the mission. Hunger, starvation, health consequences, tools, recipes, combat, placement and inventory are physical constraints/capabilities. The mod does not encode `if hungry eat`, `if attacked fight`, `gather wood`, shelter construction, progression or relationship rules. A skill only carries out the model-selected legal attempt and reports what Minecraft actually did.

The model may bring pretrained general Minecraft knowledge, but it is not told that a current block, entity, or item exists unless the local perception showed it. A block type is never labelled valuable, an entity is never labelled friend or danger, and affordance ordering is neutral rather than resource-first. A hypothesis the model forms from training or reflection remains subjective until Minecraft produces a factual outcome.

Conversation is likewise an affordance, not a dialogue policy: residents can only address a locally perceived, currently offered player or resident, and the embodiment records what was actually said.

Search is likewise an attempt, not an oracle: a resident physically visits a small bounded sequence of loaded places and can still fail to find something that exists elsewhere. Water flotation only prevents a body from being stranded by its locomotion; it introduces no survival preference. Death remains factual; an operator may explicitly use `/ec revive <name>`, which restores a body while retaining that inhabitant's stable identity.

Continuity is persistent information and selective recall, not a claim that model weights change. Minecraft-authored events remain factual source records. A resident's beliefs, self-description, projects, reflections, and autobiographical chapters are explicitly subjective and may be wrong; they are never upgraded to world truth merely because a model wrote them.

One configured local model serves the whole world. This is shared inference capacity, not a shared consciousness: each resident receives only its own UUID-keyed autobiographical context and leaves separate physical and memory traces.
