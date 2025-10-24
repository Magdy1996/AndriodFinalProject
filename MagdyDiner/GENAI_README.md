GenAI Integration (OpenAI) - Quick Start

Files added:
- app/src/main/java/com/example/yumyum/genai/GenAiRepository.kt
- app/src/main/java/com/example/yumyum/genai/GenAiViewModel.kt
- app/src/main/java/com/example/yumyum/presentation/genai/GenAiDemoScreen.kt

How it works:
- `GenAiRepository` performs a minimal HTTP POST to the OpenAI completions endpoint using HttpURLConnection.
- `GenAiViewModel` calls the repository and exposes a `result` StateFlow with the raw response.
- `GenAiDemoScreen` is a simple Compose screen where you can paste your OpenAI API key and a prompt and press Generate.

Important security note:
- Do NOT hardcode API keys in source. For testing, paste your key into the demo screen or provide it via BuildConfig or a secure vault.

Example payload used (inside `GenAiRepository`):
{"model":"text-davinci-003","prompt":"<your prompt>","max_tokens":150}

Limitations and next steps:
- The repository returns the raw JSON string. In a real app, parse the response and map to a model.
- Add network error handling, retries, and rate-limit backoff.
- Consider using provider SDKs (OpenAI Java client) or server-side proxies to keep the API key secret.

To test locally:
1. Run the app on a device or emulator.
2. Open the GenAI demo screen (add a navigation route if necessary).
3. Paste your OpenAI API key and an example prompt, press Generate, and inspect the raw response.

