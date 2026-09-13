# Upstream research ledger

Research clones live only in ignored `.upstream-research/`.

| Project | inspected commit | license | source inspected | decision |
| --- | --- | --- | --- | --- |
| [AIBot](https://github.com/zoyluoblue/mc_aiplayer) | `a029fa6a3760fd0f83834c104051b041d986da60` | MIT | `task/Task.java`, `AbstractTask.java`, `TaskState.java`, `TaskManager.java`, `BotTickCoordinator.java`, `CraftTask.java`, `BuildTask.java`, `CombatTask.java`, `CombatCore.java`, `ContainerTask.java` | Ported/adapted lifecycle only; rejected assigned-goal and survival policies. |
| [AnimaFabric](https://github.com/MapleAries/AnimaFabric) | `3e81669cae1bd12d6793044309e19c45fc265eae` | MIT | `WorldPerception.kt`, `ActionExecutor.kt`, `ActionResultClassifier.kt`, `GameThreadDispatcher.kt`, `TaskPlanner.kt` | Modern API and postcondition reference; no policy copied. |
| [Frens](https://github.com/wcfcarolina13/Frens) | `a2ffd7ac8f16f355dbf82dc8102f6dffc005173f` | MIT | inventory/body survival implementation and README | Physics/reference only; existing bounded stores retained. |
| [Mindcraft](https://github.com/mindcraft-bots/mindcraft) | `5f3acc87b479864124173de444f31fa5538f94a6` | MIT | `profiles/andy-4.json`, `profiles/andy-4-reasoning.json`, `src/models/ollama.js`, `src/agent/commands/index.js`, `src/agent/commands/actions.js`, `src/agent/action_manager.js` | Adapted chat request shape, think-block cleanup, strict command detection/arity/type concepts, and command-document style into `brain/src/andy.ts`, `profiles.ts`, and `provider.ts`. No Mineflayer action, code execution, owner role, survival mode, or goal policy is imported. |
| [Citizens-Aware](https://github.com/omarzanji/citizens-aware) | `3f567f3a01cab574716525e28a6e40827a8eb023` | MIT | repository source/README | Architecture reference only. |
| [SecondBrain](https://github.com/sailex428/SecondBrain) | `1b201547b72e5f6278e83fe45da687ec598e6730` | LGPL-3.0 | license and repository structure | Ideas only; no source copied. |

Comparison: EmergentCraft keeps native Villager navigation and bounded model-authored activities. AIBot’s reusable lifecycle was more explicit (pause/cancel/failure/progress), so that mechanical boundary was adapted. Its externally assigned task/survival behavior was deliberately excluded.
