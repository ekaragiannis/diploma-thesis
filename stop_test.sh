docker ps -q --filter "ancestor=produce-messages" | xargs -r docker stop
