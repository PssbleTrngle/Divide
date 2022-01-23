scoreboard objectives add health health "Health"
scoreboard objectives setdisplay list health
scoreboard objectives add deaths deathCount "Deaths"
scoreboard objectives add playerKills playerKillCount "Player Kills"
scoreboard objectives add totalKills totalKillCount "Total Kills"
scoreboard objectives add damage_taken custom:damage_taken "Damage Taken"
scoreboard objectives add damage_dealt custom:damage_dealt "Damage Dealt"
scoreboard objectives add jumps custom:jump "Jumps"

scoreboard objectives add crafting_tables_crafted minecraft.crafted:minecraft.crafting_table "Crafting Tables Crafted"
scoreboard objectives add crafting_tables_placed minecraft.used:minecraft.crafting_table "Crafting Tables Placed"
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