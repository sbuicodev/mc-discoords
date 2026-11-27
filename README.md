# Discoords

A Fabric mod that connects a Minecraft server to a Discord channel for coordinates: **`/dc <name>`** posts where you're standing to the channel, so everyone can find it later.

Minecraft 1.21.11, 26.1, 26.2 and 26.3, on Fabric. It runs on the server (or in singleplayer); players don't need it.

## Posting coordinates

```
/dc Base
```

posts this message to the channel:

> 🧭 Base: `120, 64, -340` (Overworld) ~ *[Steve]*

- The position is rounded to whole blocks.
- The dimension is written as Overworld, Nether or The End; other dimensions by their id (`mymod:mining`).
- The player in `*[...]*` is the one who posted it.
- Every player can use `/dc`.

## Setup

### 1. Create the bot

1. Open the [Discord Developer Portal](https://discord.com/developers/applications) and create an application.
2. Under **Bot**, click **Reset Token** and copy the token. Keep it secret: anyone with it controls the bot.
3. Under **OAuth2 → URL Generator**, pick the `bot` scope and these permissions: **View Channels** and **Send Messages**. Open the generated URL to invite the bot to your server.

### 2. Find the ids

In Discord, turn on **User Settings → Advanced → Developer Mode**. Then right-click the server icon and choose **Copy Server ID**, and right-click the channel and choose **Copy Channel ID**.

### 3. Install and configure

1. Put the Discoords jar in the server's `mods` folder, with [Fabric API](https://modrinth.com/mod/fabric-api).
2. Start the server once. Discoords writes `config/discoords.json` in the server folder; the log says the token is invalid until you fill it in. The server keeps running either way, without `/dc`.
3. Fill in `token`, `guild` and `channel`.
4. Restart the server. The log shows `Connected to channel "#..." on guild "..."` when it worked, and `/dc` is available.

## Configuration

`config/discoords.json`, read when the server starts:

```json
{
  "token": "your_bot_token_here",
  "guild": "your_guild_id_here",
  "channel": "your_channel_id_here"
}
```

| Option | Meaning |
|---|---|
| `token` | The bot's token. |
| `guild` | The Discord server's id. |
| `channel` | The id of the channel coordinates are posted to. |

Changes apply when the server restarts.

## Building

```bash
./gradlew build
```

The jars end up in `versions/<minecraft>/build/libs/`; `./gradlew buildAndCollect` copies them all to `build/libs/<version>/`.

## AI usage disclaimer

> [!WARNING]
> No generative AI was used to create Discoords' assets, and AI was not used to develop its features. AI is used as an assistant, for things like:
>
> - setting up the GitHub workflows that build and release the mod
> - migrating the build to Stonecutter, to support several Minecraft versions
> - writing the initial version of this README
> - other minor changes
>
> Every change is thoroughly reviewed by a human.

## License

CC0 1.0
