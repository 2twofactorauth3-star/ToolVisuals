from pathlib import Path
import hashlib
import io
import json
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def bundle(root=ROOT):
    from dependencies import download
    directory = root / 'libs/imagecodecs'
    manifest = json.loads((directory / 'codecs.json').read_text(encoding='utf-8'))
    files = {}
    for row in manifest['libraries']:
        path = download(row['url'], directory / row['file'], 'sha512', row['sha512'], row['bytes'])
        with zipfile.ZipFile(path) as source:
            for name in source.namelist():
                if name.endswith('/') or name.startswith('META-INF/') or name == 'module-info.class':
                    continue
                data = source.read(name)
                if name in files and files[name] != data:
                    raise ValueError('Conflicting image codec entry: ' + name)
                files[name] = data
    files['LICENSE_TwelveMonkeys'] = (directory / 'LICENSE').read_bytes()
    files['META-INF/services/javax.imageio.spi.ImageReaderSpi'] = b'com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi\n'
    files['fabric.mod.json'] = json.dumps({'schemaVersion': 1, 'id': 'toolvisuals_imagecodecs', 'version': manifest['version'],
        'name': 'ToolVisuals Image Codecs', 'environment': 'client', 'license': manifest['license']}).encode()
    output = io.BytesIO()
    with zipfile.ZipFile(output, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as target:
        for name, data in sorted(files.items()):
            target.writestr(zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0)), data, compress_type=zipfile.ZIP_DEFLATED)
    path = root / 'build/components/imagecodecs' / ('ToolVisualsImageCodecs-' + manifest['version'] + '.jar')
    path.parent.mkdir(parents=True, exist_ok=True)
    data = output.getvalue()
    if not path.is_file() or path.read_bytes() != data:
        temporary = None
        try:
            with tempfile.NamedTemporaryFile(dir=path.parent, suffix='.tmp', delete=False) as stream:
                temporary = Path(stream.name)
                stream.write(data)
            temporary.replace(path)
        finally:
            if temporary is not None:
                temporary.unlink(missing_ok=True)
    return path


def describe(root=ROOT):
    path = bundle(root)
    with zipfile.ZipFile(path) as source:
        version = json.loads(source.read('fabric.mod.json'))['version']
    return {'project': 'imagecodecs', 'file': path.name, 'version': version, 'bytes': path.stat().st_size,
        'sha512': hashlib.sha512(path.read_bytes()).hexdigest(), 'source': 'libs/imagecodecs/codecs.json'}


if __name__ == '__main__':
    print('IMAGE_CODECS_READY', bundle())
