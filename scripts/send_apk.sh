#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="$(dirname "$0")/../.env"
if [ -f "$ENV_FILE" ]; then
    set -a
    # shellcheck source=/dev/null
    source "$ENV_FILE"
    set +a
else
    echo ".env file not found at $ENV_FILE"
    exit 1
fi

APK_PATH=$1
APP_NAME=${2:-"App"}
VARIANT=${3:-"debug"}

if [ -z "${TELEGRAM_BOT_TOKEN:-}" ] || [ -z "${TELEGRAM_CHAT_ID:-}" ]; then
    echo "TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID must be defined in .env"
    exit 1
fi

if [ ! -f "$APK_PATH" ]; then
    echo "APK file not found at $APK_PATH"
    exit 1
fi

APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
CAPTION="🚀 Nuevo APK de $APP_NAME ($VARIANT)
📦 $(basename "$APK_PATH") · $APK_SIZE
📅 $(date '+%Y-%m-%d %H:%M:%S')"

echo "Sending $VARIANT APK to Telegram..."
if ! response=$(curl --silent --show-error --fail-with-body \
    -F "chat_id=$TELEGRAM_CHAT_ID" \
    -F "document=@$APK_PATH" \
    -F "caption=$CAPTION" \
    "https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendDocument" 2>&1); then
    echo "Telegram upload failed:"
    echo "$response"
    exit 1
fi

if [[ "$response" != *'"ok":true'* ]]; then
    echo "Telegram API did not confirm the upload:"
    echo "$response"
    exit 1
fi

echo "Telegram upload completed."
