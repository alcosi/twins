#!/bin/bash
cfg=$1
echo "$cfg"
while IFS= read -r line; do
    while IFS='=' read -r uuid replacement; do
        line=${line//$uuid/$replacement}
    done < "$cfg"
    echo "$line"
done
