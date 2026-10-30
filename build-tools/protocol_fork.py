from pathlib import Path
import hashlib
import json
import shutil
import zipfile
from dependencies import download

ROOT = Path(__file__).resolve().parents[1]


def component(root=ROOT):
    return root / 'components/viafabricplus'


def manifest(root=ROOT):
    return json.loads((component(root) / 'upstream.json').read_text(encoding='utf-8'))


def profile(version, root=ROOT):
    return manifest(root)['targets'][version]


def directory(version, root=ROOT):
    return root / 'build/components/viafabricplus' / version


def artifact(version, root=ROOT):
    return directory(version, root) / ('ToolVisualsProtocols-' + profile(version, root)['version'] + '+toolvisuals.' + str(manifest(root)['revision']) + '.jar')


def source_digest(root=ROOT):
    folder = component(root)
    paths = [folder / name for name in ['upstream.json', 'NOTICE', 'COPYING']]
    paths += list((folder / 'src/main/java').rglob('*.java'))
    digest = hashlib.sha256()
    for path in sorted(paths, key=lambda path: path.relative_to(folder).as_posix()):
        digest.update(path.relative_to(folder).as_posix().encode())
        digest.update(b'\0')
        digest.update(path.read_text(encoding='utf-8').replace('\r\n', '\n').encode('utf-8'))
        digest.update(b'\0')
    return digest.hexdigest()


def acquire_inputs(version, root=ROOT):
    row = profile(version, root)
    results = []
    inputs = [('binary', row['binary']), ('sources', row['sources'])] + [('sources', entry) for entry in row.get('related_sources', [])]
    for kind, entry in inputs:
        if Path(entry['file']).name != entry['file']:
            raise ValueError('Invalid protocol input filename')
        target = directory(version, root) / 'upstream' / entry['file']
        if not target.is_file() and kind == 'binary':
            candidates = [root / 'versions' / version / 'libs' / entry['file'], root / 'build/local-dependencies' / entry['file']]
            for cached in candidates:
                if cached.is_file() and cached.stat().st_size == entry['bytes'] and hashlib.sha512(cached.read_bytes()).hexdigest() == entry['sha512']:
                    target.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copyfile(cached, target)
                    break
        download(entry['url'], target, 'sha512', entry['sha512'], entry['bytes'])
        results.append(target)
    return results


def describe(version, root=ROOT):
    path = artifact(version, root)
    if not path.is_file():
        raise ValueError('Build the ViaFabricPlus fork component for ' + version + ' before generating runtime resources')
    with zipfile.ZipFile(path) as jar:
        provenance = json.loads(jar.read('META-INF/toolvisuals/protocol-component.json'))
        metadata = json.loads(jar.read('fabric.mod.json'))
    if provenance['upstream'] != profile(version, root) or provenance['patch_sources_sha256'] != source_digest(root):
        raise ValueError('The protocol component is stale; rebuild ' + version)
    return {'project': 'viafabricplus', 'file': path.name, 'version': metadata['version'], 'bytes': path.stat().st_size,
            'sha512': hashlib.sha512(path.read_bytes()).hexdigest(), 'source': 'components/viafabricplus/upstream.json',
            'upstream': profile(version, root)['binary']}


def resolve_rows(version, rows, root=ROOT):
    return [describe(version, root) if row['project'] == 'viafabricplus' else row for row in rows]


def archive_for(version, row, root=ROOT):
    return artifact(version, root) if row['project'] == 'viafabricplus' else root / 'versions' / version / 'libs' / row['file']
