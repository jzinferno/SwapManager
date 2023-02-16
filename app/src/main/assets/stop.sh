#!/system/bin/sh

SWAPFILE="/data/local/jzinferno/swapfile"

grep "$SWAPFILE" /proc/swaps
if [ $? -eq 0 ]; then
    swapoff $SWAPFILE
fi

rm -rf $SWAPFILE "$SWAPFILE"_size

exit 0