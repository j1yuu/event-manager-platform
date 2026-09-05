docker-up:
	docker compose --env-file .env -f docker/docker-compose.yml up -d

docker-down:
	docker compose --env-file .env -f docker/docker-compose.yml down

docker-delete:
	docker compose --env-file .env -f docker/docker-compose.yml down -v

docker-logs:
	docker compose --env-file .env -f docker/docker-compose.yml logs -f

manager-up:
	set -a && . ./.env && set +a && ./gradlew event-manager:bootRun

notificator-up:
	set -a && . ./.env && set +a && ./gradlew event-notificator:bootRun