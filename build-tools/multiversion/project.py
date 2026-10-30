from pathlib import Path
import sys

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'build/versions'
sys.path.insert(0,str(ROOT/'build-tools'))
