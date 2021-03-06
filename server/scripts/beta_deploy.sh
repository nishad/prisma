#!/usr/bin/env bash

TOKEN=${GITHUB_TOKEN}

echo "Fetching cb binary..."
curl --header "Authorization: token ${TOKEN}" \
     --header 'Accept: application/vnd.github.v3.raw' \
     --location "https://api.github.com/repos/graphcool/coolbelt/releases/latest" -sSL | \
     jq '.assets[] | select(.name == "coolbelt_linux") | .url' | \
     xargs -I "{}" \
         curl -sSL --header 'Accept: application/octet-stream' -o cb \
         --location "{}?access_token=${TOKEN}"

chmod +x cb

echo "Replacing images..."
export CB_MODE=env
./cb service replace-all --customer graphcool --cluster db-beta-ew1-dev
