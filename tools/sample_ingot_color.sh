#!/usr/bin/env bash
# Samples the alpha-weighted average color of a texture's opaque pixels and prints it as an
# 0xFFRRGGBB hex value, ready to paste into MoltenFluidSpec#withApparentColor(...).
#
# Usage:
#   tools/sample_ingot_color.sh path/to/texture.png
#   tools/sample_ingot_color.sh path/to/some.jar assets/modid/textures/item/thing.png
#
# Requires ImageMagick (`magick` on PATH).
set -euo pipefail

if [ "$#" -eq 1 ]; then
    texture="$1"
elif [ "$#" -eq 2 ]; then
    jar="$1"
    internal_path="$2"
    texture="$(mktemp --suffix=.png)"
    trap 'rm -f "$texture"' EXIT
    unzip -p "$jar" "$internal_path" > "$texture"
else
    echo "Usage: $0 <texture.png>" >&2
    echo "   or: $0 <mod.jar> <internal/path/to/texture.png>" >&2
    exit 1
fi

magick "$texture" -format %c histogram:info:- | \
    grep -oE '[0-9]+: \([0-9]+,[0-9]+,[0-9]+,[0-9]+\)' | \
    awk -F'[:(),]' '
        { count = $1; r = $3; g = $4; b = $5; a = $6
          if (a + 0 > 200) { wr += r * count; wg += g * count; wb += b * count; wc += count } }
        END {
            if (wc == 0) { print "no opaque pixels found" > "/dev/stderr"; exit 1 }
            printf "0xFF%02X%02X%02X\n", wr / wc, wg / wc, wb / wc
        }'
