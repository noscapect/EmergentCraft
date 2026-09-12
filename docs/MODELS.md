# Local model profiles

The Brain remains loopback-only. A provider is transport; a profile is the compact model-facing representation and sampling policy. Neither grants Minecraft privileges: the mod constructs perception, offers candidate IDs, and revalidates every consequence.

## Generic Qwen through Ollama

Set `EC_PROVIDER=ollama`, `OLLAMA_BASE_URL=http://127.0.0.1:11434`, `OLLAMA_MODEL=<installed-qwen-tag>`, and `EC_MODEL_PROFILE=qwen`. Ollama discovery uses `GET /api/tags`; startup never pulls a model.

## Andy-4 through Ollama

For this experiment, run `ollama pull sweaterdog/andy-4`, then set `OLLAMA_MODEL=sweaterdog/andy-4` and `EC_MODEL_PROFILE=minecraft-andy4`. Startup verifies the configured tag through `/api/tags` and fails clearly instead of falling back. The profile defaults to 16K context, temperature 0.4, top-p 0.9, min-p 0.05, and repeat penalty 1.15.

## Andy-4.1 through a local OpenAI-compatible server

Run an explicitly chosen local LM Studio or llama.cpp server on loopback, then set `EC_PROVIDER=openai-compatible`, `LOCAL_OPENAI_BASE_URL=http://127.0.0.1:1234/v1`, `LOCAL_OPENAI_MODEL=<loaded-Andy-4.1-model-id>`, and `EC_MODEL_PROFILE=minecraft-andy41`. An empty `LOCAL_OPENAI_API_KEY` sends no Authorization header. Discovery uses `GET /v1/models`; decisions use `POST /v1/chat/completions` with JSON Schema where supported.

## One world, one configured brain

`brain/.env` selects one provider/model/profile for the entire running server. Every resident uses that inference engine, including background consolidation, but its agent UUID continues to isolate episodes, beliefs, reflections, projects, social history and self-model. Change the global configuration by editing `.env` and restarting the Brain. `/ec brainstatus` displays only the global provider/model/profile and queue status.

Andy family names and availability are upstream/model-host dependent. EmergentCraft does not distribute weights or claim a registry tag for Andy-4.1.
