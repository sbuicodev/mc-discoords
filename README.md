# Discoords

A Fabric mod that connects a Minecraft server to a Discord channel for coordinates: **`/dc <name>`** posts where you're standing to the channel, so everyone can find it later.

Minecraft 1.21.11, 26.1, 26.2 and 26.3, on Fabric. It runs on the server (or in singleplayer); players don't need it.

## Posting coordinates

```
/dc Base
```

posts this message to the channel, with Steve's name and head as the author:

> 🧭 Base: `120, 64, -340`

- The position is rounded to whole blocks.
- Coordinates in the Overworld have no dimension. Others are written as Nether or The End, and other dimensions by their id (`(mymod:mining)`).
- Every player can use `/dc`.
- Mentions are turned off, so `/dc @everyone` doesn't ping anyone.

Without a webhook, the bot posts instead, and the player is written at the end: `🧭 Base: \`120, 64, -340\` ~ *[Steve]*`. The same happens through the webhook for players whose name contains "discord" or "clyde", since Discord doesn't accept those as author names.

## Setup

### 1. Create the webhook

1. In Discord, open the channel's settings, then **Integrations → Webhooks → New Webhook**.
2. Click **Copy Webhook URL**. Keep it secret: anyone with it can post to the channel.

### 2. Install and configure

1. Put the Discoords jar in the server's `mods` folder, with [Fabric API](https://modrinth.com/mod/fabric-api).
2. Start the server once. Discoords writes `config/discoords.json` in the server folder. The server keeps running either way, without `/dc` until Discoords is set up.
3. Fill in `webhook`.
4. Restart the server. The log shows `Posting to webhook ...` when it worked, and `/dc` is available.

### Posting through a bot (deprecated)

> [!NOTE]
> Posting through a bot will be removed in a future version. It's used only when `webhook` is empty, and the server log warns about it at startup.

1. Open the [Discord Developer Portal](https://discord.com/developers/applications) and create an application.
2. Under **Bot**, click **Reset Token** and copy the token. Keep it secret: anyone with it controls the bot.
3. Under **OAuth2 → URL Generator**, pick the `bot` scope and these permissions: **View Channels** and **Send Messages**. Open the generated URL to invite the bot to your server.
4. In Discord, turn on **User Settings → Advanced → Developer Mode**. Then right-click the server icon and choose **Copy Server ID**, and right-click the channel and choose **Copy Channel ID**.
5. Fill in `token`, `guild` and `channel`, leave `webhook` empty, and restart the server. The log shows `Posting to channel "#..." on guild "..."` when it worked.

## Configuration

`config/discoords.json`, read when the server starts:

```json
{
  "token": "your_bot_token_here",
  "guild": "your_guild_id_here",
  "channel": "your_channel_id_here",
  "webhook": ""
}
```

| Option | Meaning |
|---|---|
| `webhook` | The webhook URL coordinates are posted through. When it's set, the other options are ignored. |
| `token` | The bot's token (deprecated). |
| `guild` | The Discord server's id (deprecated). |
| `channel` | The id of the channel the bot posts to (deprecated). |

Changes apply when the server restarts. Config files from older versions get `webhook` added the next time the server starts.

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
> - writing the unit tests
> - other minor changes
>
> Every change is thoroughly reviewed by a human.

## License

CC0 1.0
