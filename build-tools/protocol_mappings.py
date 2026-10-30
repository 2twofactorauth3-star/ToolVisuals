from pathlib import Path
import hashlib
import json
import re
import zipfile
from dependencies import download


def descriptor(value, classes):
    dimensions = 0
    while value.endswith('[]'): dimensions += 1; value = value[:-2]
    primitives = {'void': 'V', 'boolean': 'Z', 'byte': 'B', 'char': 'C', 'short': 'S', 'int': 'I', 'long': 'J', 'float': 'F', 'double': 'D'}
    return '[' * dimensions + primitives.get(value, 'L' + classes.get(value, value).replace('.', '/') + ';')


def prepare(version, root):
    directory = root / 'build/components/viafabricplus/games' / version
    output = directory / 'named.tiny'
    toolchain = root / 'build/components/viafabricplus/toolchain'
    if version == '1.21.4':
        with zipfile.ZipFile(toolchain / 'yarn-1.21.4+build.8-v2.jar') as source:
            output.write_bytes(source.read('mappings/mappings.tiny'))
        return output
    data = json.loads((root / 'components/viafabricplus/game-inputs.json').read_text())['targets'][version]
    classes = {}; members = {}; sources = []
    for kind in ['server', 'client']:
        row = data['mappings'][kind]
        path = download(row['url'], directory / (kind + '-mappings.txt'), 'sha1', row['sha1'], row.get('size'))
        lines = path.read_text(encoding='utf-8').splitlines(); sources.append(lines)
        for line in lines:
            if line and not line.startswith((' ', '#')) and line.endswith(':'):
                name, obfuscated = line[:-1].split(' -> '); classes[name] = obfuscated
    for lines in sources:
        owner = None
        for line in lines:
            if not line or line.startswith('#'): continue
            if not line.startswith(' '): owner = line[:-1].split(' -> ')[1].replace('.', '/'); continue
            if ' -> ' not in line: continue
            declaration, obfuscated = line.strip().split(' -> ')
            declaration = re.sub(r'^(?:\d+:)+', '', declaration)
            if '(' in declaration:
                match = re.fullmatch(r'(\S+) (\S+)\((.*?)\)(?::\d+)*', declaration)
                if match is None: raise ValueError('Invalid official method mapping')
                returns, name, arguments = match.groups()
                signature = '(' + ''.join(descriptor(argument, classes) for argument in arguments.split(',') if argument) + ')' + descriptor(returns, classes)
                members[owner, 'm', obfuscated, signature] = name
            else:
                value, name = declaration.split(' '); members[owner, 'f', obfuscated, descriptor(value, classes)] = name
    reverse = {value.replace('.', '/'): name.replace('.', '/') for name, value in classes.items()}
    with zipfile.ZipFile(toolchain / ('intermediary-' + version + '-v2.jar')) as source:
        intermediary = source.read('mappings/mappings.tiny').decode().splitlines()
    aliases = {}
    for line in intermediary:
        if line.startswith('c\t'):
            _, obfuscated, intermediate = line.split('\t'); aliases[obfuscated] = intermediate
    def remap(signature): return re.sub(r'L([^;]+);', lambda match: 'L' + aliases.get(match[1], match[1]) + ';', signature)
    result = ['tiny\t2\t0\tintermediary\tnamed']; owner = None
    for line in intermediary:
        parts = line.split('\t')
        if line.startswith('c\t'):
            _, owner, intermediate = parts; result.append('c\t' + intermediate + '\t' + reverse.get(owner, intermediate))
        elif line.startswith(('\tf\t', '\tm\t')):
            _, kind, signature, obfuscated, intermediate = parts
            result.append('\t' + kind + '\t' + remap(signature) + '\t' + intermediate + '\t' + members.get((owner, kind, obfuscated, signature), intermediate))
    output.write_text('\n'.join(result) + '\n', encoding='utf-8')
    return output


def map_access(path, mappings, destination):
    classes = {}; fields = {}; methods = {}; owner = None
    for line in mappings.read_text(encoding='utf-8').splitlines():
        parts = line.split('\t')
        if line.startswith('c\t'): owner = parts[1]; classes[owner] = parts[2]
        elif line.startswith('\tf\t'): fields[owner, parts[3], parts[2]] = parts[4]
        elif line.startswith('\tm\t'): methods[owner, parts[3], parts[2]] = parts[4]
    def remap(signature): return re.sub(r'L([^;]+);', lambda match: 'L' + classes.get(match[1], match[1]) + ';', signature)
    lines = []
    for line in path.read_text(encoding='utf-8').splitlines():
        if not line.strip(): lines.append(line); continue
        parts = line.split()
        if parts[0] in {'accessWidener', 'classTweaker'}: parts[2] = 'named'
        elif len(parts) >= 3:
            owner = parts[2]; parts[2] = classes.get(owner, owner)
            if len(parts) >= 5:
                table = fields if parts[1] == 'field' else methods
                name = table.get((owner, parts[3], parts[4]))
                if name is None:
                    candidates = {target for (_, member, signature), target in table.items() if member == parts[3] and signature == parts[4]}
                    if len(candidates) == 1: name = next(iter(candidates))
                parts[3] = name or parts[3]; parts[4] = remap(parts[4])
        lines.append(' '.join(parts))
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text('\n'.join(lines) + '\n', encoding='utf-8')
    return destination
