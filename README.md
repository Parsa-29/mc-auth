# Auth Plugin for Minecraft

## Overview
The **Auth** plugin is a Minecraft plugin that provides user authentication features, allowing players to register, login, and manage their account security. This plugin is designed to enhance the security of your Minecraft server by adding an additional layer of protection.

## Features
- **Register:** Allows players to create a new account.
- **Login:** Allows players to log into their existing account.
- **Security:** Enables or disables IP-based security protection.
- **Config:** Allows administrators to change plugin configurations.
- **Change Password:** Allows players to change their password.

## Installation
1. Download the plugin JAR file.
2. Place the JAR file in the `plugins` directory of your Minecraft server.
3. Restart your Minecraft server to load the plugin.

## Commands
The following commands are available with the **Auth** plugin:

### Register
- **Description:** Register a new account
- **Usage:** `/register <password> <confirmPassword>`
- **Aliases:** `r`, `reg`

### Login
- **Description:** Login to your account
- **Usage:** `/login <password>`
- **Aliases:** `l`, `log`

### Security
- **Description:** Turn on/off security IP protection
- **Usage:** `/security <on/off>`
- **Aliases:** `sec`

### Config
- **Description:** Change the configuration of the plugin
- **Usage:** `/config`
- **Aliases:** `c`, `cfg`

### Change Password
- **Description:** Change your password
- **Usage:** `/changepass <oldPassword> <newPassword> <confirmPassword>`
- **Aliases:** `cp`, `chp`

## Configuration
The plugin can be configured using the `config.yml` file. Below is the default configuration:

```yaml
# Auth plugin for Minecraft by Khvicha Parsadanasvili
# Auth configuration file

# The number of failed login attempts before the player is kicked
MaxAttempts: 3

# The minimum length of the password
MinPasswordLength: 6

# The time in seconds before the player is kicked
KickTime: 15

# Plugin prefix
Prefix: "§9[§bAuth§9]§c "
