# DiscordLink

---
A Velocity plugin by the _pvphub.me dev team_.

### What is it?
**DiscordLink** is a plugin that allows your players to link their minecraft and discord accounts.
The plugin is able to handle multiple accounts to one user, as well as unlinking users.

### Features
 - *Role Sync* Allows you to synchronize luckperms ranks with discord roles. (setup below)
 - *Discord Commands*
 - *Absolute customization*
 - *RGB support*

---
### Setup
>#### Role sync
>You can set up role sync in `config.yml` near the bottom.
> 
>`enabled: {boolean}` Will allow this feature to function.
> 
>`server-id: {long id}` Lets you configure 
> 
> ```yaml 
> mapping:
>   {group}: {long id}
> ```
> The `{group}` should be the name of the group, and the `{long id}` should be the id of the role you want this group to point to.
> 
> `rank-sync-auto: {int}` How often shall we automatically sync roles (seconds) 
> `-1` will disable it.

>####Other
> `code-expire: {int}` How long should a code take to expire (seconds)
> 
> `catch-code: {boolean}` If the player sends their verification code in chat, should we catch it?
> 
> `verify-whole-command: {boolean}` When a player clicks the `%code%`, should we make them copy the command or just the code

>####Remote SQL
> Take a look at LuckPerms documentation, we function similarly, though we will be using sqlite.
---
>####API
>The plugin has some features such as an event system, that supports:
> 
> `VerifySuccessEvent`, `VerifyFailEvent` and `CodeExpireEvent`
> 
> You can use the `VerifyEvents` class to register a new `VerifyListener`.
> 
This plugin was developed for `pvphub.me` - Join today!