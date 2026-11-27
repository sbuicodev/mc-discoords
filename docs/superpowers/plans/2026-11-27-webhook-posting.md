# Webhook Posting Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `/dc` posts through a configured webhook as the player (name and head), falls back to the bot with a deprecation warning, and never writes the Overworld.

**Architecture:** `DiscordService` stays the only class that talks to Discord and is built either for the bot (JDA) or for the webhook (`JDAWebhookClient`). Text building is split into small static classes (`CoordinatesMessage`, `MessageSignature`, `WebhookMessages`) so it is testable without Minecraft or Discord. Webhook posting is tested through an OkHttp client whose interceptor answers in place of Discord.

**Tech Stack:** Java 21/25, Fabric, Stonecutter, JDA 6.2.0, `club.minnced:discord-webhooks:0.8.4`, JUnit 5.7.1.

**Spec:** `docs/superpowers/specs/2026-11-27-webhook-posting-design.md`

## Global Constraints

- Never run the game (`runClient`, `runServer`). Verify with `./gradlew build` and `./gradlew test` only.
- Keep the existing style: same packages, 4-space indent, public fields on `Config`, `null` on failure logged by `Discoords`, few comments, small classes.
- The webhook URL and its token never appear in a log line.
- Mentions are turned off in both modes.
- The Overworld is never written in a message; other dimensions are written as `DimensionNames.of` writes them.
- Avatar URL: `https://mc-heads.net/avatar/<player name>`.
- Author names containing `discord` or `clyde` (any case) are not sent as the webhook username; the text gets the ` ~ *[name]*` suffix instead.
- No Discord calls in tests.

## Review Focus

- A `/dc` name containing `@everyone` or `@here`: no ping in either mode (mentions turned off in the request).
- A config file from 1.1.x (no `webhook` key): loads, keeps its values, gains `"webhook": ""`, stays in bot mode.
- A `webhook` value with spaces around it or blank: treated as not set.
- Discord answering an error (404, 429 after retries): `/dc` still answers the player, the error is logged without the token, nothing is thrown on the server thread.
- Server stop in webhook mode: the client is closed, so its threads don't keep the server alive.

## Predicted files

```
build.gradle.kts                          ~ discord-webhooks moves to bundled
CHANGELOG.md                              ~ ## 1.2.0
README.md                                 ~ webhook setup, `webhook` option, tests in the AI list
src/main/java/net/hawkelele/discoords/
├── Discoords.java                        ~ picks the mode, deprecation warning, closes on server stop
├── commands/
│   ├── CoordinatesMessage.java           + the /dc text
│   └── SendCoordinatesCommand.java       ~ uses CoordinatesMessage
├── config/
│   ├── Config.java                       ~ `webhook`, usesWebhook()
│   └── ConfigProvider.java               ~ constructor taking the file, writes missing keys back
└── services/
    ├── DiscordService.java               ~ bot or webhook, sendMessage(text, player), describe(), close()
    ├── MessageSignature.java             + " ~ *[player]*"
    └── WebhookMessages.java              + the webhook message
src/test/java/net/hawkelele/discoords/
├── commands/CoordinatesMessageTest.java  +
├── config/ConfigTest.java                +
├── config/ConfigProviderTest.java        +
├── services/DiscordServiceTest.java      +
├── services/MessageSignatureTest.java    +
└── services/WebhookMessagesTest.java     +
```

---

### Task 1: The /dc text

**Files:**
- Create: `src/main/java/net/hawkelele/discoords/commands/CoordinatesMessage.java`
- Create: `src/main/java/net/hawkelele/discoords/services/MessageSignature.java`
- Modify: `src/main/java/net/hawkelele/discoords/commands/SendCoordinatesCommand.java`
- Modify: `src/main/java/net/hawkelele/discoords/services/DiscordService.java` (temporary: `sendMessage(String text, String player)` posting `MessageSignature.of(text, player)` through the bot, so the build stays green)
- Test: `src/test/java/net/hawkelele/discoords/commands/CoordinatesMessageTest.java`, `src/test/java/net/hawkelele/discoords/services/MessageSignatureTest.java`

**Interfaces:**
- Produces: `CoordinatesMessage.coordinates(double x, double y, double z) -> String` (`"120, 64, -340"`); `CoordinatesMessage.of(String name, double x, double y, double z, String dimension) -> String` where `dimension` is the raw id (`"minecraft:overworld"`); `MessageSignature.of(String text, String player) -> String`; `DiscordService.sendMessage(String text, String player)`.

- [ ] **Step 1: Write the failing tests**

```java
// CoordinatesMessageTest
assertEquals("120, 64, -340", CoordinatesMessage.coordinates(120.4, 63.6, -340.4));
assertEquals("🧭 Base: `120, 64, -340`", CoordinatesMessage.of("Base", 120, 64, -340, "minecraft:overworld"));
assertEquals("🧭 Fortress: `10, 70, 20` (Nether)", CoordinatesMessage.of("Fortress", 10, 70, 20, "minecraft:the_nether"));
assertEquals("🧭 Mine: `1, 2, 3` (mymod:mining)", CoordinatesMessage.of("Mine", 1, 2, 3, "mymod:mining"));
// MessageSignatureTest
assertEquals("🧭 Base: `1, 2, 3` ~ *[Steve]*", MessageSignature.of("🧭 Base: `1, 2, 3`", "Steve"));
```

- [ ] **Step 2: Run them to see them fail**

Run: `./gradlew test --tests '*CoordinatesMessageTest' --tests '*MessageSignatureTest'`
Expected: compilation fails, classes missing.

- [ ] **Step 3: Implement both classes** as final classes with a private constructor, like `DimensionNames`. `of` uses `coordinates` and `DimensionNames.of`, and appends ` (<dimension>)` only when the id isn't `minecraft:overworld`.

- [ ] **Step 4: `SendCoordinatesCommand` builds the text with `CoordinatesMessage.of(name, pos.x, pos.y, pos.z, <dimension id>)` and calls `discord.sendMessage(text, player.getName().getString())`; the success message uses `CoordinatesMessage.coordinates`. `DiscordService.sendMessage(text, player)` posts `MessageSignature.of(text, player)` through the bot.**

- [ ] **Step 5: Run the tests**

Run: `./gradlew test`
Expected: all pass, including `DimensionNamesTest`.

- [ ] **Step 6: Commit** `feat: /dc no longer writes the Overworld; message text in its own class`

### Task 2: The `webhook` config option

**Files:**
- Modify: `src/main/java/net/hawkelele/discoords/config/Config.java`, `src/main/java/net/hawkelele/discoords/config/ConfigProvider.java`
- Test: `src/test/java/net/hawkelele/discoords/config/ConfigTest.java`, `src/test/java/net/hawkelele/discoords/config/ConfigProviderTest.java`

**Interfaces:**
- Produces: `Config.webhook` (`String`, default `""`); `Config.usesWebhook() -> boolean`; `new ConfigProvider(File file)`; `new ConfigProvider()` still uses `config/discoords.json`.

- [ ] **Step 1: Write the failing tests**

```java
// ConfigTest
Config config = new Config();
assertFalse(config.usesWebhook());
config.webhook = "   "; assertFalse(config.usesWebhook());
config.webhook = null; assertFalse(config.usesWebhook());
config.webhook = "https://discord.com/api/webhooks/1/abc"; assertTrue(config.usesWebhook());
// ConfigProviderTest, with @TempDir Path dir
// createsDefaultFile: load() on a missing dir/config/discoords.json returns the defaults, and the file exists afterwards with "webhook"
// readsExistingFile: a file with token "t", guild "g", channel "c", webhook "w" loads those four values
// addsMissingWebhookKey: a file with only token/guild/channel loads with webhook "", keeps the three values, and the file now contains "\"webhook\""
```

- [ ] **Step 2: Run them to see them fail**

Run: `./gradlew test --tests '*ConfigTest' --tests '*ConfigProviderTest'`
Expected: compilation fails (`webhook`, `usesWebhook`, `ConfigProvider(File)` missing).

- [ ] **Step 3: Implement.** `load()` writes the config back after reading an existing file, so new keys appear; a `null` read is returned as before without writing.

- [ ] **Step 4: Run the tests**

Run: `./gradlew test`
Expected: all pass.

- [ ] **Step 5: Commit** `feat: "webhook" config option, added to existing config files`

### Task 3: Posting through the webhook

**Files:**
- Modify: `build.gradle.kts` (`implementation("club.minnced:discord-webhooks:0.8.4")` becomes `bundled(...)`)
- Create: `src/main/java/net/hawkelele/discoords/services/WebhookMessages.java`
- Modify: `src/main/java/net/hawkelele/discoords/services/DiscordService.java`
- Test: `src/test/java/net/hawkelele/discoords/services/WebhookMessagesTest.java`, `src/test/java/net/hawkelele/discoords/services/DiscordServiceTest.java`

**Interfaces:**
- Consumes: `MessageSignature.of(String, String)`.
- Produces: `WebhookMessages.of(String text, String player) -> WebhookMessage`; `DiscordService.connectWebhook(String url) -> DiscordService` (throws `IllegalArgumentException` for a URL that isn't a Discord webhook); package-private `DiscordService.connectWebhook(String url, OkHttpClient http)`; `DiscordService.sendMessage(String text, String player) -> CompletableFuture<Void>` (completes normally even when Discord fails); `DiscordService.describe() -> String` (`channel "#name" on guild "name"` or `webhook <id>`); `DiscordService.close()`.

- [ ] **Step 1: Write the failing tests**

```java
// WebhookMessagesTest
WebhookMessage message = WebhookMessages.of("🧭 Base: `1, 2, 3`", "Steve");
assertEquals("Steve", message.getUsername());
assertEquals("https://mc-heads.net/avatar/Steve", message.getAvatarUrl());
assertEquals("🧭 Base: `1, 2, 3`", message.getContent());
// bodyTurnsMentionsOff: the JSON from message.getBody() (read through an okio Buffer) has allowed_mentions.parse == []
// forbiddenNames: for "discordfan" and "Mr_CLYDE", getUsername() is null and getContent() == MessageSignature.of(text, name); avatar is still set

// DiscordServiceTest — an OkHttpClient whose interceptor records the request and answers 204 (or 404 with body "Unknown Webhook")
// postsAsThePlayer: connectWebhook("https://discord.com/api/webhooks/123/secret-token", http).sendMessage("hi @everyone", "Steve").join();
//   request is POST, path ends with "/webhooks/123/secret-token", body JSON has content "hi @everyone", username "Steve",
//   avatar_url "https://mc-heads.net/avatar/Steve", allowed_mentions.parse []
// failureDoesNotThrow: with the 404 interceptor, sendMessage(...).join() returns normally
// failureTextHasNoToken: DiscordService.failureText(<the 404 exception from the client>) contains "404" and not "secret-token"
// rejectsOtherUrls: assertThrows(IllegalArgumentException.class, () -> DiscordService.connectWebhook("https://example.com/hook"))
// describesWebhookById: describe() == "webhook 123"
```

- [ ] **Step 2: Run them to see them fail**

Run: `./gradlew test --tests '*WebhookMessagesTest' --tests '*DiscordServiceTest'`
Expected: compilation fails.

- [ ] **Step 3: Implement.**
  - `WebhookMessages.of` uses `WebhookMessageBuilder` with `AllowedMentions.none()`; a name is forbidden when its lower case contains `discord` or `clyde`.
  - `DiscordService` keeps `TextChannel channel` and adds `JDAWebhookClient webhook`; exactly one is set.
  - The webhook client is built with `new WebhookClientBuilder(url).setHttpClient(http).setWait(false).setDaemon(true).setAllowedMentions(AllowedMentions.none()).buildJDA()` (`http` may be `null` in production).
  - Bot sending adds `.setAllowedMentions(Collections.emptyList())`.
  - Webhook failures are logged as `Couldn't post to the webhook: {}` with package-private `failureText(Throwable)` (the unwrapped cause's message; `HttpException` messages hold only the status and Discord's body).
  - `close()` closes the webhook client; nothing for the bot.

- [ ] **Step 4: Run the tests**

Run: `./gradlew test`
Expected: all pass.

- [ ] **Step 5: Check the jar bundles the library**

Run: `./gradlew build && unzip -l versions/1.21.11/build/libs/discoords-1.1.2+1.21.11.jar | grep -i 'discord-webhooks\|json'`
Expected: `META-INF/jars/discord-webhooks-0.8.4.jar` and an `org.json` jar listed.

- [ ] **Step 6: Commit** `feat: post through a webhook as the player`

### Task 4: Choosing the mode at startup

**Files:**
- Modify: `src/main/java/net/hawkelele/discoords/Discoords.java`

**Interfaces:**
- Consumes: `Config.usesWebhook()`, `DiscordService.connectWebhook(String)`, `DiscordService.connect(...)`, `describe()`, `close()`.

- [ ] **Step 1: Implement `initDiscord()`.**
  - When `config.usesWebhook()`: try `connectWebhook(config.webhook.trim())`. On `IllegalArgumentException`, log the error `"webhook" in {} isn't a Discord webhook URL; posting through the bot instead` (no URL) and continue to the bot.
  - Bot path: log the warning `Posting through the bot is deprecated and will be removed in a future version; set "webhook" in {}`, then connect as today.
  - After connecting, log `Posting to {}` with `describe()`, replacing the channel/guild line.
  - Register `ServerLifecycleEvents.SERVER_STOPPING` to call `discord.close()`.

- [ ] **Step 2: Build every version**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL for all four nodes, tests included.

- [ ] **Step 3: Commit** `feat: webhook mode at startup, bot posting deprecated`

### Task 5: Docs

**Files:**
- Modify: `README.md`, `CHANGELOG.md`

- [ ] **Step 1: README.**
  - Example without `(Overworld)`, posted as the player.
  - Setup gets "Create the webhook" (channel settings → Integrations → Webhooks → New Webhook → Copy Webhook URL), with the bot section marked deprecated.
  - Config example and table gain `webhook`.
  - The note that the webhook URL is a secret like the token.
  - The AI disclaimer list gains `- writing the unit tests`.

- [ ] **Step 2: CHANGELOG `## 1.2.0`.**
  - Added: webhook posting as the player.
  - Changed: the Overworld no longer written.
  - Deprecated: bot posting.
  - Fixed: `/dc` can no longer ping.

- [ ] **Step 3: Commit** `docs: webhook setup, 1.2.0 changelog, AI-written tests in the disclaimer`
