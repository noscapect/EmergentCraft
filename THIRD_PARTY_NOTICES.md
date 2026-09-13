# Third-party notices

## Mindcraft Andy command adapter (adapted)

Source project: https://github.com/mindcraft-bots/mindcraft

Source commit: `5f3acc87b479864124173de444f31fa5538f94a6`

Original paths: `src/models/ollama.js`, `src/agent/commands/index.js`, `src/agent/commands/actions.js`, and the Andy profile JSON files.

EmergentCraft destinations: `brain/src/andy.ts`, `brain/src/profiles.ts`, and `brain/src/provider.ts`.

Method: ADAPTED. The Ollama chat shape, private-think cleanup strategy, strict command parsing/validation concepts, and command-document presentation were reimplemented in TypeScript. No source action function is executed or imported. Model text can only match an already-current offered action; arbitrary JavaScript, Mineflayer APIs, owner instructions, survival modes, and external goals are excluded.

License: MIT License, Copyright (c) 2024 Kolby Nottingham.

```text
MIT License

Copyright (c) 2024 Kolby Nottingham

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## AIBot task lifecycle (adapted)

Source project: https://github.com/zoyluoblue/mc_aiplayer

Source commit: `a029fa6a3760fd0f83834c104051b041d986da60`

Original paths: `src/main/java/io/github/zoyluo/aibot/task/Task.java`, `AbstractTask.java`, and `TaskState.java`.

EmergentCraft destinations: `mod/src/main/java/nl/noscapect/emergentcraft/task/ActivityTask.java`, `AbstractActivityTask.java`, and `TaskState.java`.

Method: ADAPTED. AIPlayer and action-pack dependencies were removed; no policy, task planner, navigation, combat, crafting, or survival logic was copied.

License: MIT License, Copyright (c) 2026 zoyluo. The required notice is retained in the source headers.

```text
MIT License

Copyright (c) 2026 zoyluo

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
