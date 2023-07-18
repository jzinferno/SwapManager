#!/system/bin/sh

SWAPDIR="/data/local/jzinferno"
SWAPFILE="$SWAPDIR/swapfile"

grep $SWAPFILE "/proc/swaps"
if [ ! $? -eq 0 ]; then
    rm -rf $SWAPFILE
fi

if [ ! -f $SWAPFILE ]; then
    mkdir -p $SWAPDIR
    fallocate -l "$1"G $SWAPFILE
    chmod 600 $SWAPFILE
    mkswap $SWAPFILE
fi

if [ -f $SWAPFILE ]; then
    grep $SWAPFILE "/proc/swaps"
    if [ ! $? -eq 0 ]; then
        swapon $SWAPFILE
        if [ ! $? -eq 0 ]; then
            exit 1
        fi
    fi
fi

exit 0