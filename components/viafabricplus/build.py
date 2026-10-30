from pathlib import Path
import argparse
import sys

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'build-tools'))
from protocol_fork import acquire_inputs, build_component

parser = argparse.ArgumentParser()
parser.add_argument('version', choices=['26.3', '1.21.11', '1.21.4'])
actions = parser.add_mutually_exclusive_group(required=True)
actions.add_argument('--acquire', action='store_true')
actions.add_argument('--build', action='store_true')
args = parser.parse_args()
if args.acquire:
    paths = acquire_inputs(args.version)
    print('PROTOCOL_FORK_INPUTS_READY', args.version, len(paths), flush=True)
else:
    print('PROTOCOL_FORK_BUILT', args.version, build_component(args.version), flush=True)
