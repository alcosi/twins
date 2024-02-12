#!/bin/bash
while IFS= read -r line; do
    while IFS='=' read -r uuid replacement; do
        line=${line//$uuid/$replacement}
    done < "logSubstitution.conf"
    echo "$line"
done
