from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
PROPERTIES = dict(line.strip().split('=', 1) for line in (ROOT / 'gradle.properties').read_text(encoding='utf-8').splitlines() if '=' in line)
VERSION = PROPERTIES['mod_version']
LIBRARIES = ROOT / 'build/libs'
CLIENT_STEM = f'{PROPERTIES["archives_base_name"]}-{VERSION}+fabric-{PROPERTIES["minecraft_version"]}'
CLIENT = LIBRARIES / f'{CLIENT_STEM}.jar'
TARGETS = json.loads((ROOT / 'versions/targets.json').read_text(encoding='utf-8'))['targets']
CLIENTS = {target['minecraft']: LIBRARIES / f'{PROPERTIES["archives_base_name"]}-{VERSION}+fabric-{target["minecraft"]}.jar' for target in TARGETS}
SERVER = LIBRARIES / f'{PROPERTIES["plugin_archives_base_name"]}-{VERSION}.jar'

import hashlib


def pinned_music(root):
    directory = root / 'libs/music'
    manifest_file = directory / 'music-helper.json'
    manifest = json.loads(manifest_file.read_text(encoding='utf-8'))
    source = root / 'native/music/windows/MediaBridge.cs'
    archive = directory / 'ToolVisualsMedia.exe'
    data = archive.read_bytes()
    if manifest['protocol'] != 1 or manifest['file'] != archive.name or manifest['bytes'] != len(data):
        raise RuntimeError('Invalid bundled Windows music helper manifest')
    if not 1024 <= len(data) <= 131072 or data[:2] != b'MZ' or hashlib.sha256(data).hexdigest() != manifest['sha256']:
        raise RuntimeError('Bundled Windows music helper checksum mismatch')
    if hashlib.sha256(source.read_bytes()).hexdigest() != manifest['source_sha256']:
        raise RuntimeError('Rebuild Windows music helper after changing its source')
    expected = [source, *sorted((source.parent / 'audio').glob('*.cs'))]
    actual = [{'path': path.relative_to(root).as_posix(), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()} for path in expected]
    if manifest.get('sources') != actual:
        raise RuntimeError('Bundled Windows music helper sources do not match its manifest')
    return manifest_file, archive
