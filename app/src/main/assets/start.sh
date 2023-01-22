#!/system/bin/sh

# shellcheck disable=SC2034
SWAPDIR="/data/local/jzinferno"
SWAPFILE=$SWAPDIR"/swapfile"

mkdir -p $SWAPDIR

if [ ! -f $SWAPFILE ]; then
    fallocate -l "$1"G $SWAPFILE
    chmod 600 $SWAPFILE
    mkswap $SWAPFILE
    echo "$1" > "$SWAPFILE"_size
fi

if [ -f $SWAPFILE ]; then
    grep $SWAPFILE "/proc/swaps"
    if [ $? = 0 ]; then
        exit 0
    else
        swapon $SWAPFILE
    fi
fi

exit 0