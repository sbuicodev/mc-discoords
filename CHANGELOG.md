# Changelog

Each section is named after the release tag it belongs to (`## <mod.version>`).
The release workflow publishes the section matching the pushed tag as the GitHub release notes.

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
