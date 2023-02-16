#!/system/bin/sh

# shellcheck disable=SC2046
SWAPFILE="/data/local/jzinferno/swapfile"

if ! [ -f $SWAPFILE ]; then
    mkdir -p $(dirname "$SWAPFILE")
    fallocate -l "$1"G $SWAPFILE
    chmod 600 $SWAPFILE
    mkswap $SWAPFILE
    echo "$1" > "$SWAPFILE"_size
fi

grep "$SWAPFILE" /proc/swaps
if [ $? -eq 0 ]; then
    true
else
    swapon $SWAPFILE
fi

exit 0