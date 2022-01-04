bossbar set game:peace players @a
execute store result bossbar game:peace value run scoreboard players get PEACE_COOLDOWN values

function game:vote/tick
function game:deals/health/tick

team join all @a[team=!all,gamemode=!spectator]

execute if score FROZEN values matches 1 run function game:frozen

execute if entity @a[tag=admin,scores={pause=1..}] run function game:pause
execute if entity @a[tag=admin,scores={resume=1..}] run function game:resume
execute if entity @a[tag=admin,scores={freeze=1..}] run function game:freeze
execute if entity @a[tag=admin,scores={peace=1}] run function game:mode/peace
execute if entity @a[tag=admin,scores={peace=2..}] run function game:mode/temp_peace
execute if entity @a[tag=admin,scores={war=1..}] run function game:mode/war

scoreboard players reset @a pause
scoreboard players reset @a freeze
scoreboard players reset @a resume
scoreboard players reset @a peace
scoreboard players reset @a war

scoreboard players enable @a[tag=admin] pause
scoreboard players enable @a[tag=admin] freeze
scoreboard players enable @a[tag=admin] resume
scoreboard players enable @a[tag=admin] peace
scoreboard players enable @a[tag=admin] war