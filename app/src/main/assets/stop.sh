#!/system/bin/sh

SWAPFILE=/data/local/jzinferno/swapfile

if [ -f $SWAPFILE ]; then
    if grep -q $SWAPFILE /proc/swaps; then
        if ! swapoff $SWAPFILE; then
            exit 1
        fi
    fi
    if ! rm -rf $SWAPFILE; then
        exit 1
    fi
fi

exit 0