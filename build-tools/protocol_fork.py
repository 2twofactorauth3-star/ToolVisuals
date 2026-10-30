from pathlib import Path
import hashlib
import json
import os
import re
import shutil
import subprocess
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


def java_tools():
    candidates = []
    if os.environ.get('JAVA_HOME'):
        candidates.append(Path(os.environ['JAVA_HOME']) / 'bin' / ('javac.exe' if os.name == 'nt' else 'javac'))
    detected = shutil.which('javac')
    if detected:
        candidates.append(Path(detected))
    for compiler in candidates:
        if not compiler.is_file():
            continue
        result = subprocess.run([str(compiler), '-version'], capture_output=True, text=True, encoding='utf-8', errors='replace')
        match = re.search(r'javac (\d+)', result.stdout + result.stderr)
        if result.returncode == 0 and match and int(match[1]) >= 25:
            return compiler.with_name('java.exe' if os.name == 'nt' else 'java'), compiler
    raise ValueError('JDK 25 or newer is required to prepare the protocol component. Set JAVA_HOME or add its bin directory to PATH.')


def build_component(version, root=ROOT):
    try:
        describe(version, root)
        return artifact(version, root)
    except (ValueError, OSError, zipfile.BadZipFile, KeyError, json.JSONDecodeError):
        pass
    original = acquire_inputs(version, root)[0]
    java, compiler = java_tools()
    tools = root / 'build/components/viafabricplus/tools'
    classes = tools / 'classes'
    classes.mkdir(parents=True, exist_ok=True)
    libraries = []
    for row in manifest(root)['tool_dependencies']:
        target = tools / 'libraries' / row['file']
        cached = root / 'build/local-dependencies' / row['file']
        if not target.exists() and cached.is_file() and cached.stat().st_size == row['bytes'] and hashlib.sha512(cached.read_bytes()).hexdigest() == row['sha512']:
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(cached, target)
        download(row['url'], target, 'sha512', row['sha512'], row['bytes'])
        libraries.append(target)
    classpath = os.pathsep.join(map(str, libraries))
    stamp = tools / 'source-digest.txt'
    fingerprint = source_digest(root)
    if not stamp.is_file() or stamp.read_text().strip() != fingerprint or not (classes / 'ru/he3hauka/toolvisuals/protocols/build/ForkBuilder.class').is_file():
        sources = sorted((component(root) / 'src/main/java').rglob('*.java'))
        subprocess.run([str(compiler), '-encoding', 'UTF-8', '--release', '25', '-proc:none', '-classpath', classpath, '-d', str(classes), *map(str, sources)], cwd=root, check=True)
        stamp.write_text(fingerprint + '\n')
    subprocess.run([str(java), '-cp', os.pathsep.join([str(classes), classpath]), 'ru.he3hauka.toolvisuals.protocols.build.ForkBuilder', str(original), str(component(root)), version, str(artifact(version, root))], cwd=root, check=True)
    describe(version, root)
    return artifact(version, root)


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
