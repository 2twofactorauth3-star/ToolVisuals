from pathlib import Path
import hashlib
import json
import os
import re
import shutil
import subprocess
import zipfile
import contextlib
import time
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
    paths = [folder / name for name in ['upstream.json', 'libraries.json', 'toolchain.json', 'game-inputs.json', 'NOTICE', 'COPYING', 'ATTRIBUTION', 'build.gradle', 'build.py']]
    paths += [root / 'build-tools' / name for name in ['protocol_fork.py', 'protocol_source.py', 'protocol_mappings.py', 'dependencies.py']]
    paths.append(root / 'versions/targets.json')
    paths += [path for path in (folder / 'src').rglob('*') if path.is_file()]
    digest = hashlib.sha256()
    for path in sorted(paths, key=lambda path: path.relative_to(root).as_posix()):
        digest.update(path.relative_to(root).as_posix().encode())
        digest.update(b'\0')
        data = path.read_bytes()
        if b'\0' not in data:
            data = data.replace(b'\r\n', b'\n')
        digest.update(data)
        digest.update(b'\0')
    return digest.hexdigest()


def acquire_inputs(version, root=ROOT):
    from protocol_source import acquire
    return acquire(version, root)


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
    with build_lock(root):
        try:
            describe(version, root)
            return artifact(version, root)
        except (ValueError, OSError, zipfile.BadZipFile, KeyError, json.JSONDecodeError):
            pass
        from protocol_source import build
        result = build(version, root)
        describe(version, root)
        return result


@contextlib.contextmanager
def build_lock(root):
    path = root / 'build/components/viafabricplus/source-build.lock'
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open('a+b') as stream:
        stream.seek(0, 2)
        if stream.tell() == 0: stream.write(b'0'); stream.flush()
        started = time.monotonic()
        while True:
            try:
                stream.seek(0)
                if os.name == 'nt':
                    import msvcrt
                    msvcrt.locking(stream.fileno(), msvcrt.LK_NBLCK, 1)
                else:
                    import fcntl
                    fcntl.flock(stream.fileno(), fcntl.LOCK_EX | fcntl.LOCK_NB)
                break
            except OSError:
                if time.monotonic() - started > 900: raise TimeoutError('Timed out waiting for the protocol source compiler')
                time.sleep(0.1)
        try: yield
        finally:
            stream.seek(0)
            if os.name == 'nt': msvcrt.locking(stream.fileno(), msvcrt.LK_UNLCK, 1)
            else: fcntl.flock(stream.fileno(), fcntl.LOCK_UN)


def describe(version, root=ROOT):
    path = artifact(version, root)
    if not path.is_file():
        raise ValueError('Build the ViaFabricPlus fork component for ' + version + ' before generating runtime resources')
    with zipfile.ZipFile(path) as jar:
        provenance = json.loads(jar.read('META-INF/toolvisuals/protocol-component.json'))
        metadata = json.loads(jar.read('fabric.mod.json'))
    if provenance['upstream'] != profile(version, root) or provenance['source_tree_sha256'] != source_digest(root):
        raise ValueError('The protocol component is stale; rebuild ' + version)
    api = directory(version, root) / 'compile-api.jar'
    if not api.is_file() or provenance['compile_api_sha512'] != hashlib.sha512(api.read_bytes()).hexdigest():
        raise ValueError('The protocol compilation API is missing or stale; rebuild ' + version)
    return {'project': 'viafabricplus', 'file': path.name, 'version': metadata['version'], 'bytes': path.stat().st_size,
            'sha512': hashlib.sha512(path.read_bytes()).hexdigest(), 'source': 'components/viafabricplus/upstream.json',
            'upstream': {'version': profile(version, root)['version'], 'source': profile(version, root)['sources']}}


def resolve_rows(version, rows, root=ROOT):
    from image_codecs import describe as image_codecs
    return [describe(version, root) if row['project'] == 'viafabricplus' else row for row in rows if row['project'] != 'imagecodecs'] + [image_codecs(root)]


def archive_for(version, row, root=ROOT):
    if row['project'] == 'imagecodecs':
        return root / 'build/components/imagecodecs' / row['file']
    return artifact(version, root) if row['project'] == 'viafabricplus' else root / 'versions' / version / 'libs' / row['file']
