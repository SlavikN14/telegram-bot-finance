rootProject.name = "tg-finance-tracker"

include(
    "shared-api",
    "telegram-bot",
    "finance-service",
    "finance-service:domain",
    "finance-service:application",
    "finance-service:infrastructure",
)
