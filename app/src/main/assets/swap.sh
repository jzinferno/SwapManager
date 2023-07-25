#!/system/bin/sh

LOGFILE="$(realpath "$(dirname "$0")")/log.txt"
SWAPFILE=/data/local/jzinferno/swapfile

log_swap() {
  if [ -z "$2" ]; then
    echo "$LOGFILE"
  elif [ "$2" = "--clear" ]; then
    echo "Device: $(uname -a)" > "$LOGFILE"
    echo >> "$LOGFILE"
  else
    if [[ "$1" = "1" || "$1" = "log" ]]; then
      shift
    fi
    echo "$(date '+%d-%m-%Y %H:%M:%S') $(whoami)@$(hostname) : $*" >> "$LOGFILE"
  fi
}

stop_swap() {
  if [ -f $SWAPFILE ]; then
    if grep -q $SWAPFILE /proc/swaps; then
      if ! swapoff $SWAPFILE; then
        log_swap 1 "Failed to stop swap file!"
        exit 1
      fi
    fi
    swapoff $SWAPFILE
    if ! rm -rf $SWAPFILE; then
      log_swap 1 "Failed to delete swap file!"
      exit 1
    fi
  fi
  log_swap 1 "Swap file stopped successfully."
}

start_swap() {
  if [ -f $SWAPFILE ]; then
    if [ "$2" = "--restart" ]; then
      swapoff $SWAPFILE
      if ! swapon $SWAPFILE; then
        log_swap 1 "Failed to start swap file!"
        exit 1
      fi
    else
      stop_swap
    fi
  else
    mkdir -p "$(dirname $SWAPFILE)"
    fallocate -l "${2}G" $SWAPFILE
    chmod 600 $SWAPFILE
    mkswap $SWAPFILE
  fi

  if [ -f $SWAPFILE ]; then
    if ! grep -q $SWAPFILE /proc/swaps; then
      if ! swapon $SWAPFILE; then
        log_swap 1 "Failed to start swap file!"
        exit 1
      fi
    fi
  else
    log_swap 1 "Failed to create swap file!"
    exit 1
  fi
  log_swap 1 "Swap file started successfully."
}

status_swap() {
  if grep -q $SWAPFILE /proc/swaps; then
    log_swap 1 "${SWAPFILE}: is running!"
  else
    log_swap 1 "${SWAPFILE}: is NOT running!"
    exit 1
  fi
}

if [ ! -f "$LOGFILE" ]; then
  log_swap 1 --clear
fi

case "$1" in
  log )
    log_swap "$@"
    ;;
  start )
    start_swap "$@"
    ;;
  stop )
    stop_swap "$@"
    ;;
  status )
    status_swap "$@"
    ;;
  * )
    echo $SWAPFILE
    ;;
esac