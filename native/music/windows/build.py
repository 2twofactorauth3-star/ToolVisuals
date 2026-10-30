from pathlib import Path
import hashlib
import json
import subprocess
import sys

root = Path(__file__).resolve().parents[3]
framework = Path('C:/Windows/Microsoft.NET/Framework64/v4.0.30319')
source = root / 'native/music/windows/MediaBridge.cs'
sources = [source, *sorted((source.parent / 'audio').glob('*.cs'))]
destination = root / 'libs/music'
destination.mkdir(exist_ok=True)
output = destination / 'ToolVisualsMedia.exe'
runtime = next(Path('C:/Windows/Microsoft.NET/assembly/GAC_MSIL/System.Runtime').glob('*/System.Runtime.dll'))
interop = next(Path('C:/Windows/Microsoft.NET/assembly/GAC_MSIL/System.Runtime.InteropServices.WindowsRuntime').glob('*/System.Runtime.InteropServices.WindowsRuntime.dll'))
references = [runtime, interop, framework / 'System.Web.Extensions.dll']
references += [Path('C:/Windows/System32/WinMetadata') / ('Windows.' + name + '.winmd') for name in ['Foundation', 'Media', 'Storage']]
result = subprocess.run([str(framework / 'csc.exe'), '/nologo', '/utf8output', '/optimize+', '/target:winexe', '/platform:anycpu',
                         '/out:' + str(output), *['/reference:' + str(path) for path in references], *map(str, sources)],
                        cwd=root, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
sys.stdout.buffer.write(result.stdout)
if result.returncode:
    raise SystemExit(result.returncode)
manifest = {'protocol': 1, 'file': output.name, 'bytes': output.stat().st_size, 'sha256': hashlib.sha256(output.read_bytes()).hexdigest(),
            'source_sha256': hashlib.sha256(source.read_bytes()).hexdigest(), 'runtime': '.NET Framework 4.5+', 'minimum_windows': '10 1809',
            'sources': [{'path': path.relative_to(root).as_posix(), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()} for path in sources]}
(destination / 'music-helper.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
print('MUSIC_HELPER_BUILD_OK', output.stat().st_size)
