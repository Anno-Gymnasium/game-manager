# game-manager-neu
## (Partially complete)
This is a school project from my German "Informatik-Leistungskurs" (Computer Science advanced course).

An application for registering accounts and using them to create, host or join games with several leagues, teams, scores etc.  
The history of scores and leagues is recorded.

It uses these services / Java libraries:
- An Azure SQL database for account and game management
- Java JDBI for accessing and modifying the database
- JavaFX for the GUI

Accounts and games can be customized with descriptions and multiple settings.  
A game has the following hierarchy of roles in its whitelist, with each account having one role in a game:
- Admin: Controls the progress of the game and decides who can join. An admin assigns roles to involved accounts via whitelist.
- Team leader: Manages his team, e.g. team name, description and whitelist.
- Player: Belongs to the game and is able to change his name / description specifically for the game.
- Spectator: Can only view the game and its history.

An account can not view or modify a game if it is not included in the game's whitelist and the game is not set to public.

Users can request specific roles in public games. Admins of a game are able to invite other users to their games under a specific role.

The 3 main types of game are:
- Tree game: The number of teams is halved with each new league by comparing the scores for each match of 2 teams.
- Matching game: Teams in a league are arranged in temporary matches and can be rearranged after evaluating these matches.
- Matchless game: Teams have no match scores. Instead, the league scores for each team are modified directly.

If the game is not a tree game, an admin can specify a minimum score teams have to reach in order to be qualified for the next league.  
Alternatively, a percentage can be specified. Only this percentage of best teams will be qualified.
