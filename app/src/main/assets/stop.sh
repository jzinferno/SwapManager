#!/system/bin/sh

SWAPFILE="/data/local/jzinferno/swapfile"

if [ -f $SWAPFILE ]; then
    grep $SWAPFILE "/proc/swaps"
    if [ $? -eq 0 ]; then
        swapoff $SWAPFILE
        if [ ! $? -eq 0 ]; then
            exit 1
        fi
    fi
    rm -rf $SWAPFILE
fi

exit 0