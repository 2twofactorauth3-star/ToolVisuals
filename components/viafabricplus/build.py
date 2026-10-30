from pathlib import Path
import argparse
import sys

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'build-tools'))
from protocol_fork import acquire_inputs

parser = argparse.ArgumentParser()
parser.add_argument('version', choices=['26.3', '1.21.11', '1.21.4'])
parser.add_argument('--acquire', action='store_true', required=True)
args = parser.parse_args()
paths = acquire_inputs(args.version)
print('PROTOCOL_FORK_INPUTS_READY', args.version, len(paths), flush=True)
