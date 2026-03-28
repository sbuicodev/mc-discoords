# Discoords

Send your current Minecraft coordinates to a Discord channel.

## Setup

### Prerequisites

- Java 21+
- Minecraft 26.1
- Fabric Loader 0.18.5+
- A Discord bot token
- A Discord channel ID

### Configuration

Create a `config/discoords.json` file in your Minecraft directory:

```json
{
  "token": "YOUR_DISCORD_BOT_TOKEN",
  "guild": "GUILD_NAME",
  "channel": "CHANNEL_ID"
}
```

### Getting a Discord Bot Token

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application
3. Go to the "Bot" section
4. Click "Reset Token" to generate a token
5. Copy and save the token securely

### Inviting the Bot to Your Server

1. Go to OAuth2 → URL Generator
2. Select scopes: `bot`
3. Select permissions: `Send Messages`, `Read Message History`
4. Use the generated URL to invite the bot

## Usage

Run `/coords` in-game to send your current position to the configured Discord channel.

## Building

```bash
./gradlew build
```

The built jar will be in `build/libs/`.
