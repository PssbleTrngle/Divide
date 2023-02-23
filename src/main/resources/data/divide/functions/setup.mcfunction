scoreboard objectives add health divide_health "Health"
scoreboard objectives setdisplay list divide_health
scoreboard objectives add divide_deaths deathCount "Deaths"
scoreboard objectives add divide_player_kills playerKillCount "Player Kills"
scoreboard objectives add divide_total_kills totalKillCount "Total Kills"
scoreboard objectives add divide_damage_taken custom:damage_taken "Damage Taken"
scoreboard objectives add divide_damage_dealt custom:damage_dealt "Damage Dealt"
scoreboard objectives add divide_jumps custom:jump "Jumps"

scoreboard objectives add divide_crafting_tables_crafted minecraft.crafted:minecraft.crafting_table "Crafting Tables Crafted"
scoreboard objectives add divide_crafting_tables_placed minecraft.used:minecraft.crafting_table "Crafting Tables Placed"

scoreboard objectives add divide_per_player_info dummy "INFO"
scoreboard objectives setdisplay sidebar divide_per_player_info

gamerule doDaylightCycle false
time set noon

gamerule doLimitedCrafting true
gamerule playersSleepingPercentage 30
gamerule doInsomnia false
gamerule doImmediateRespawn true
gamerule doTraderSpawning false
gamerule doPatrolSpawning false