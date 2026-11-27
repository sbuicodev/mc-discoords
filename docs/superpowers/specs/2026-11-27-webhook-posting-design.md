# Webhook posting

## Why

`/dc` messages should look like the player wrote them: the player's name as the author, their head as the avatar.
Discoords stays a mod that only posts. Reading, editing and deleting belong to the future discoords-api.

## Goals

- With `webhook` set in the config, `/dc` posts through that webhook as the player.
- Without it, `/dc` posts through the bot exactly as today, and the server log warns at startup that bot posting will be removed in a future version.
- Tests that keep the internals working (message text, webhook request, config, mode choice) without calling Discord.
- The README explains the webhook setup and says AI wrote the tests.

## Non-goals

- Reading the channel, editing or deleting messages, keeping message ids.
- Removing the bot now.
- Any link to Locator Markers.

## Anti-goals

- The webhook URL ever showing up in a log line: like the bot token, it lets anyone post as the server.
- A player being able to ping the Discord server through `/dc` (`/dc @everyone`).
- `/dc` blocking the server thread while Discord answers.

## Approaches

1. **`JDAWebhookClient` from `club.minnced:discord-webhooks` 0.8.4** *(picked by you)*. Handles the request, rate limits and allowed mentions; its last update was February 2024, which is fine for one POST. It is bundled into the jar like JDA (it pulls `org.json`; OkHttp is already there through JDA). Only `withUrl` and `send(WebhookMessage)` are used, so nothing depends on JDA classes from its JDA 4 era.
2. **Java's built-in `HttpClient`, posting the JSON by hand.** No dependency, but the request, errors and rate limits are ours to write.
3. **JDA's own webhook client.** Needs a logged-in JDA instance, so it would keep the bot running in webhook mode.

## Design

**Config** (`config/discoords.json`) gains `"webhook": ""`. Empty means bot mode.
Existing config files get the new key written into them on load, so people see it. *(my call)*
In webhook mode `token`, `guild` and `channel` are ignored and the bot never connects.
A `webhook` value the library rejects as not a Discord webhook URL logs an error and falls back to bot mode. *(my call)*

**Startup in webhook mode:** one `JDAWebhookClient` is built from the URL and kept for the server's lifetime, closed when the server stops. The log says `Posting through webhook <id>` (the id only, never the token). The library has no way to look a webhook up, so a deleted webhook shows up as a logged error on the first `/dc`, not at startup. *(my call)*

**Message text**
- Webhook mode: `🧭 Base: \`120, 64, -340\``, author `Steve`, avatar `https://mc-heads.net/avatar/Steve`.
- Bot mode: `🧭 Base: \`120, 64, -340\` ~ *[Steve]*`.
- The Overworld is never written, in either mode: a message without a dimension is in the Overworld. Other dimensions are written as today, `(Nether)`, `(The End)`, `(mymod:mining)`. discoords-api will treat a missing dimension as the Overworld.
- The avatar uses the player's name, so it works on offline-mode servers too. *(my call)*
- Discord rejects webhook author names containing "discord" or "clyde". For those players the author is left to the webhook's own name and the text keeps the `~ *[name]*` suffix, so who posted stays visible. *(my call)*

**Mentions are turned off in both modes** (`AllowedMentions.none()` for the webhook, the same for the bot). This also fixes `/dc @everyone` in bot mode today.

**Sending is asynchronous.** A failed webhook post (non-2xx or network error) is logged with the status and Discord's message, never the URL.

**Code shape: the existing style and organization, kept as close as possible**
- Same packages (`commands`, `config`, `services`, `registry`, `exceptions`), same habits: 4-space indent, public fields on `Config`, `null` returned on failure and logged by `Discoords`, few comments, small classes.
- Splitting code into more pieces is fine where it helps, especially for testing (for example the webhook message building out of `DiscordService`), as long as the pieces read like the rest of the code.
- `DiscordService` stays the one class that talks to Discord, the way the manual attempt started: `connect(token, guild, channel)` for the bot, a new `connectWebhook(url)` for the webhook, and `sendMessage(...)` that posts the right way for the mode it was built with. No new interfaces unless a test needs one.
- A test-only way to build the webhook `DiscordService` with its own OkHttp client (package-private).
- The message text moves out of `SendCoordinatesCommand` into a small final class in `commands` with a static method, like `DimensionNames`, so it is testable without Minecraft types.
- `Discoords.initDiscord()` picks the mode from the config and logs the deprecation warning, like it logs the token error today.
- `ConfigProvider` gains a constructor taking the file, the current one keeps `config/discoords.json`.
- `discord-webhooks` moves from `implementation` to `bundled` in `build.gradle.kts`, so it ships in the jar.

**Predicted files** (`+` new, `~` changed, the rest untouched):

```
build.gradle.kts                          ~ discord-webhooks moves to bundled
CHANGELOG.md                              ~ ## 1.2.0
README.md                                 ~ webhook setup, `webhook` option, tests in the AI list
src/main/java/net/hawkelele/discoords/
├── Discoords.java                        ~ picks the mode, deprecation warning, closes on server stop
├── commands/
│   ├── Command.java
│   ├── CoordinatesMessage.java           + the /dc text: coordinates and dimension, Overworld left out
│   ├── DimensionNames.java
│   └── SendCoordinatesCommand.java       ~ uses CoordinatesMessage, passes the player to DiscordService
├── config/
│   ├── Config.java                       ~ `webhook` field, usesWebhook()
│   └── ConfigProvider.java               ~ constructor taking the file, writes missing keys back
├── exceptions/
│   └── DiscordServiceException.java
├── registry/
│   └── CommandRegistry.java
└── services/
    ├── DiscordService.java               ~ bot or webhook, sendMessage(text, player), close()
    ├── MessageSignature.java             + the " ~ *[player]*" suffix
    └── WebhookMessages.java              + the webhook message: author, avatar, no mentions, name fallback
src/test/java/net/hawkelele/discoords/
├── commands/
│   ├── CoordinatesMessageTest.java       +
│   └── DimensionNamesTest.java
├── config/
│   ├── ConfigTest.java                   +
│   └── ConfigProviderTest.java           +
└── services/
    ├── DiscordServiceTest.java           + webhook posting through a fake HTTP client
    ├── MessageSignatureTest.java         +
    └── WebhookMessagesTest.java          +
```

**Tests** (JUnit, no Discord):
- message text in both modes, rounding, the Overworld left out, other dimensions written, the forbidden-name fallback;
- the webhook message, including mentions turned off;
- a rejected webhook URL falling back to bot mode;
- mode choice from the config;
- config: default file created, existing file read, missing `webhook` key added;
- `WebhookPoster` with an OkHttp client whose interceptor answers in place of Discord (nothing leaves the machine): method, path, the JSON body the library sends, and that a failed post is logged without the token.

**Docs:** README setup gets a webhook section (channel settings → Integrations → Webhooks → New Webhook → Copy Webhook URL) and the `webhook` option, with bot posting marked as deprecated, and the `/dc` example without `(Overworld)`. The AI disclaimer list gains "writing the unit tests". CHANGELOG gets a `## 1.2.0` section; the version bump and release happen only when asked.

## Left to the builder

Class and package names beyond the ones above, the exact log wording, and how the local test server is set up.
