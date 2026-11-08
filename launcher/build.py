from pathlib import Path
import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
LAUNCHER = ROOT / 'launcher'


def bundle_clients():
    version = re.search(r'^mod_version=(.+)$', (ROOT / 'gradle.properties').read_text(), re.M).group(1).strip()
    target_versions = [target['minecraft'] for target in json.loads((ROOT / 'versions/targets.json').read_text())['targets']]
    bundle = LAUNCHER / 'bundle'
    bundle.mkdir(exist_ok=True)
    entries = []
    for minecraft in target_versions:
        source = ROOT / 'build/libs' / ('ToolVisuals-' + version + '+fabric-' + minecraft + '.jar')
        if not source.is_file():
            raise FileNotFoundError('Build the three ToolVisuals client JARs before building the launcher: ' + str(source))
        with zipfile.ZipFile(source) as jar:
            metadata = json.loads(jar.read('fabric.mod.json'))
        if metadata['id'] != 'toolvisuals' or metadata['version'] != version or metadata['depends']['minecraft'] not in [minecraft, '=' + minecraft, '~' + minecraft]:
            raise ValueError('The client archive does not match the requested native version: ' + source.name)
        digest = hashlib.sha512(source.read_bytes()).hexdigest()
        destination = bundle / source.name
        if not destination.is_file() or hashlib.sha512(destination.read_bytes()).hexdigest() != digest:
            shutil.copyfile(source, destination)
        entries.append({'minecraft': minecraft, 'file': source.name, 'version': version, 'sha512': digest, 'bytes': source.stat().st_size})
    allowed = {entry['file'] for entry in entries}
    for path in bundle.glob('*.jar'):
        if path.name not in allowed:
            if path.resolve().parent != bundle.resolve(): raise ValueError('Invalid bundle cleanup path')
            path.unlink()
    (bundle / 'clients.json').write_text(json.dumps(entries, indent=2) + '\n', encoding='utf-8')
    return entries


def build(development=False, source=None):
    entries = bundle_clients()
    environment = dict(os.environ)
    environment['DOTNET_CLI_TELEMETRY_OPTOUT'] = '1'
    environment['DOTNET_SKIP_FIRST_TIME_EXPERIENCE'] = '1'
    environment['DOTNET_GENERATE_ASPNET_CERTIFICATE'] = 'false'
    environment.setdefault('DOTNET_CLI_HOME', str(ROOT / 'build/dotnet-home'))
    environment.setdefault('NUGET_PACKAGES', str(ROOT / 'build/launcher/nuget'))
    output = ROOT / 'build/launcher' / ('development' if development else 'publish')
    command = ['dotnet', 'build' if development else 'publish', str(LAUNCHER / 'ToolLauncher.csproj'), '-c', 'Release', '--nologo', '-o', str(output), '-p:BaseIntermediateOutputPath=' + str(ROOT / 'build/launcher/obj') + os.sep]
    if not development:
        command += ['-r', 'win-x64', '--self-contained', 'true', '-p:PublishSingleFile=true', '-p:DebugType=None', '-p:DebugSymbols=false']
    if source: command += ['--source', str(Path(source).resolve())]
    subprocess.run(command, cwd=ROOT, env=environment, check=True)
    if not development:
        launcher_version = ET.parse(LAUNCHER / 'ToolLauncher.csproj').findtext('./PropertyGroup/Version')
        if not launcher_version or not re.fullmatch(r'\d+\.\d+\.\d+', launcher_version):
            raise ValueError('Provide a valid ToolLauncher project version')
        destination = ROOT / 'build/libs' / ('ToolLauncher-' + launcher_version + '-win-x64.exe')
        shutil.copyfile(output / 'ToolLauncher.exe', destination)
        print('TOOLLAUNCHER_READY', destination, destination.stat().st_size, 'clients=' + ','.join(entry['minecraft'] for entry in entries))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--development', action='store_true')
    parser.add_argument('--nuget-source')
    arguments = parser.parse_args()
    build(arguments.development, arguments.nuget_source)
