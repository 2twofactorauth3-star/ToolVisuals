import argparse
from project import ROOT
from dependencies import acquire
from sources import generate

parser=argparse.ArgumentParser()
parser.add_argument('version',choices=['1.21.4','1.21.11'])
parser.add_argument('--resources',action='store_true')
args=parser.parse_args()
acquire(args.version)
generate(args.version)
generate(args.version,True)
if args.resources:
    from resources import resources
    resources(args.version)
