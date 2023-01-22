#!/system/bin/sh

# shellcheck disable=SC2034
SWAPDIR="/data/local/jzinferno"
SWAPFILE=$SWAPDIR"/swapfile"

if [ -f $SWAPFILE ]; then
    grep $SWAPFILE "/proc/swaps"
    if [ $? = 0 ]; then
        swapoff $SWAPFILE
    fi
fi

if [ -f $SWAPFILE ]; then
    rm -rf $SWAPFILE
fi
rm -rf "$SWAPFILE"_size

exit 0