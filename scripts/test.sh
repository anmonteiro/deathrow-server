set -e

source ${BASH_SOURCE%/*}/vars.sh
lein clean
lein test
