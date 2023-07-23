#!/system/bin/sh

DIR="$(cd "$(dirname "$0")" && pwd)"
SWAPFILE=/data/local/jzinferno/swapfile

if ! sh "$DIR/stop.sh"; then
    exit 1
fi

if [ ! -f $SWAPFILE ]; then
    mkdir -p "$(dirname $SWAPFILE)"
    fallocate -l "${1}G" $SWAPFILE
    chmod 0600 $SWAPFILE
    mkswap $SWAPFILE
fi

if [ -f $SWAPFILE ]; then
    if ! grep -q $SWAPFILE /proc/swaps; then
        if ! swapon $SWAPFILE; then
            exit 1
        fi
    fi
else
    exit 1
fi

exit 0