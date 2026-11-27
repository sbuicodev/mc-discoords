# Changelog

Each section is named after the release tag it belongs to (`## <mod.version>`).
The release workflow publishes the section matching the pushed tag as the GitHub release notes.

## 1.2.1

### Added
- `/dc` can post through a webhook (`webhook` in the config), so the message shows the player's name and head as its author.
- Webhook URLs for a thread or a forum post (`?thread_id=...`) post into that thread.
- Unit tests for the message text, the config, the webhook URL and webhook posting.

### Changed
- Coordinates in the Overworld are posted without a dimension; other dimensions are still written.
- Players whose name Discord doesn't accept as an author ("discord" or "clyde" in it, or exactly "everyone" or "here") are written at the end of the message instead.
- A message still waiting on Discord's rate limit after a minute is dropped and logged, instead of being posted late.
- A failed post is logged once, on one line, without the webhook URL.
- The config file is written back only when it misses an option, and never left half written.
- The build and release workflows run the tests, so a release with a failing test isn't published.

### Deprecated
- Posting through the bot. It's used only when no webhook is set, and will be removed in a future version.

### Fixed
- `/dc` can no longer ping people, like with `/dc @everyone`.

## 1.1.2

### Fixed
- When Discord rejects the bot token, the log no longer prints the token, since server logs often get shared.

## 1.1.1

### Added
- A README with what Discoords does, how to set up the bot, and the configuration options.

### Changed
- `/dc` messages include the dimension, written as Overworld, Nether or The End (other dimensions by their id).

## 1.1.0

### Added
- Support for Minecraft 26.2 and 26.3, and 1.21.11 again: one release now covers 1.21.11, 26.1, 26.2 and 26.3.

### Fixed
- JDA and its dependencies are now bundled in the mod jar, so the mod no longer needs them installed separately.

## 1.0.1

### Changed
- Updated to Minecraft 26.1.

## 1.0.0

### Added
- `/dc <name>` command that sends the player's current coordinates to a Discord channel.
