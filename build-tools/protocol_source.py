from pathlib import Path
import concurrent.futures
import hashlib
import io
import json
import os
import re
import shutil
import subprocess
import zipfile
from dependencies import download


def read_json(path):
    return json.loads(path.read_text(encoding='utf-8'))


def fetch(root, row, destination):
    algorithm = 'sha512' if 'sha512' in row else 'sha1'
    expected = row[algorithm]
    size = row.get('bytes', row.get('size'))
    candidates = [root / 'build/local-dependencies' / destination.name,
                  root / 'build/protocol-source/raw-libraries' / destination.name,
                  root / 'build/protocol-source/tool-libraries' / destination.name]
    if row.get('path'):
        candidates.append(root / 'build/versions/libraries' / row['path'])
    if not destination.exists():
        for candidate in candidates:
            if candidate.is_file() and (size is None or candidate.stat().st_size == size) and hashlib.new(algorithm, candidate.read_bytes()).hexdigest() == expected:
                destination.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(candidate, destination)
                break
    return download(row['url'], destination, algorithm, expected, size)


def acquire(version, root):
    component = root / 'components/viafabricplus'
    cache = root / 'build/components/viafabricplus'
    libraries = read_json(component / 'libraries.json')
    toolchain = read_json(component / 'toolchain.json')['libraries']
    game = read_json(component / 'game-inputs.json')['targets'][version]
    tasks = [(libraries['libraries'][name], cache / 'libraries' / name) for name in libraries['targets'][version]]
    loader = '0.19.3' if version == '1.21.4' else '0.19.5'
    for name, row in toolchain.items():
        if name.startswith('intermediary-') and name != 'intermediary-' + version + '-v2.jar': continue
        if name.startswith('yarn-') and version != '1.21.4': continue
        if name.startswith('fabric-loader-') and name != 'fabric-loader-' + loader + '.jar': continue
        tasks.append((row, cache / 'toolchain' / name))
    tasks.append((game['client'], cache / 'games' / version / 'client.jar'))
    tasks += [(row, cache / 'games/libraries' / row['path']) for row in game['libraries']]
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        return list(pool.map(lambda item: fetch(root, *item), tasks))


def write_archive(files):
    memory = io.BytesIO()
    with zipfile.ZipFile(memory, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for name, data in sorted(files.items()):
            entry = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            entry.external_attr = 0o100644 << 16
            archive.writestr(entry, data)
    return memory.getvalue()


def write_changed(path, data):
    if path.is_file() and path.read_bytes() == data: return
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(path.name + '.tmp')
    try:
        temporary.write_bytes(data)
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)


def tool_classpath(version, root):
    component = root / 'components/viafabricplus'
    cache = root / 'build/components/viafabricplus/toolchain'
    names = read_json(component / 'toolchain.json')['libraries']
    loader = '0.19.3' if version == '1.21.4' else '0.19.5'
    return [cache / name for name in names if not name.startswith(('intermediary-', 'yarn-')) and (not name.startswith('fabric-loader-') or name == 'fabric-loader-' + loader + '.jar')]


def run_compiler(java, compiler_classpath, arguments, sources, destination, classpath, release, log):
    destination.mkdir(parents=True, exist_ok=True)
    options = ['-encoding', 'UTF-8', '-g', '--release', str(release), '-proc:none', '-Xmaxerrs', '100', '-classpath', os.pathsep.join(map(str, classpath)), '-d', str(destination)]
    arguments.write_text('\n'.join([*options, '--SOURCES--', *map(str, sources)]) + '\n', encoding='utf-8')
    with log.open('w', encoding='utf-8') as stream:
        result = subprocess.run([str(java), '-cp', compiler_classpath, 'ru.he3hauka.toolvisuals.protocols.build.SourceCompiler', str(arguments)], stdout=stream, stderr=subprocess.STDOUT)
    if result.returncode:
        raise RuntimeError('Protocol source compilation failed; see ' + str(log) + '\n' + log.read_text(encoding='utf-8', errors='replace')[-16000:])


def prepare_tools(version, root, java, javac):
    component = root / 'components/viafabricplus'
    folder = root / 'build/components/viafabricplus/source-tools'
    folder.mkdir(parents=True, exist_ok=True)
    classes = folder / 'classes'
    classes.mkdir(exist_ok=True)
    sources = sorted((component / 'src/main/java').rglob('*.java'))
    fingerprint = hashlib.sha256(b''.join(path.read_bytes().replace(b'\r\n', b'\n') for path in sources) + (component / 'toolchain.json').read_bytes().replace(b'\r\n', b'\n')).hexdigest()
    stamp = folder / 'fingerprint.txt'
    libraries = tool_classpath(version, root)
    classpath = os.pathsep.join(map(str, [classes, *libraries]))
    if not stamp.is_file() or stamp.read_text().strip() != fingerprint:
        bootstrap = next(path for path in sources if path.name == 'SourceCompiler.java')
        subprocess.run([str(javac), '-encoding', 'UTF-8', '--release', '25', '-proc:none', '-d', str(classes), str(bootstrap)], check=True)
        run_compiler(java, classpath, folder / 'compile.txt', sources, classes, libraries, 25, folder / 'compile.log')
        stamp.write_text(fingerprint + '\n')
    return classpath


def prepare_game(version, root, java, tools, game_libraries):
    folder = root / 'build/components/viafabricplus/games' / version
    game = folder / 'client.jar'
    if version != '26.3':
        mappings = folder / 'intermediary.tiny'
        mapping_jar = root / 'build/components/viafabricplus/toolchain' / ('intermediary-' + version + '-v2.jar')
        with zipfile.ZipFile(mapping_jar) as source: mappings.write_bytes(source.read('mappings/mappings.tiny'))
        intermediate = folder / 'intermediary.jar'
        signature = hashlib.sha256(game.read_bytes() + mappings.read_bytes()).hexdigest()
        stamp = folder / 'remap.txt'
        if not intermediate.is_file() or not stamp.is_file() or stamp.read_text().strip() != signature:
            game_input = extract_classes(game, folder / 'game-input')
            dependency_input = folder / 'library-input'
            dependency_signature = hashlib.sha256(b''.join(path.read_bytes() for path in game_libraries)).hexdigest()
            dependency_stamp = folder / 'libraries.txt'
            if not dependency_input.is_dir() or not dependency_stamp.is_file() or dependency_stamp.read_text().strip() != dependency_signature:
                if dependency_input.exists():
                    if dependency_input.resolve().parent != folder.resolve(): raise ValueError('Invalid protocol classpath directory')
                    shutil.rmtree(dependency_input)
                for path in game_libraries: extract_classes(path, dependency_input)
                dependency_stamp.write_text(dependency_signature + '\n')
            subprocess.run([str(java), '-Xmx2G', '-cp', tools, 'ru.he3hauka.toolvisuals.protocols.build.SourceTools', 'remap', str(game_input), str(intermediate), str(mappings), 'official', 'intermediary', 'plain', str(dependency_input)], check=True)
            stamp.write_text(signature + '\n')
        game = intermediate
        from protocol_mappings import prepare
        named_mappings = prepare(version, root)
        named = folder / 'named.jar'
        named_signature = hashlib.sha256(game.read_bytes() + named_mappings.read_bytes()).hexdigest()
        named_stamp = folder / 'named.txt'
        if not named.is_file() or not named_stamp.is_file() or named_stamp.read_text().strip() != named_signature:
            intermediate_input = extract_classes(game, folder / 'game-intermediary')
            subprocess.run([str(java), '-Xmx2G', '-cp', tools, 'ru.he3hauka.toolvisuals.protocols.build.SourceTools', 'remap', str(intermediate_input), str(named), str(named_mappings), 'intermediary', 'named', 'plain'], check=True)
            named_stamp.write_text(named_signature + '\n')
        game = named
    target_sources = root / 'components/viafabricplus/src/targets' / version
    access = sorted([*target_sources.rglob('*.accesswidener'), *target_sources.rglob('*.classtweaker')])
    if version != '26.3':
        from protocol_mappings import map_access
        access = [map_access(path, folder / 'named.tiny', folder / 'named-access' / path.relative_to(target_sources)) for path in access]
    widened = folder / 'compile.jar'
    signature = hashlib.sha256(game.read_bytes() + b''.join(path.read_bytes().replace(b'\r\n', b'\n') for path in access) + (root / 'components/viafabricplus/src/main/java/ru/he3hauka/toolvisuals/protocols/build/SourceTools.java').read_bytes().replace(b'\r\n', b'\n')).hexdigest()
    stamp = folder / 'widen.txt'
    if not widened.is_file() or not stamp.is_file() or stamp.read_text().strip() != signature:
        subprocess.run([str(java), '-Xmx2G', '-cp', tools, 'ru.he3hauka.toolvisuals.protocols.build.SourceTools', 'widen', str(game), str(widened), *map(str, access)], check=True)
        stamp.write_text(signature + '\n')
    return widened


def named_dependencies(version, root, java, tools, dependencies):
    folder = root / 'build/components/viafabricplus/games' / version
    mappings = folder / 'named.tiny'
    output = folder / 'dependencies-named.jar'
    signature = hashlib.sha256(b''.join(path.read_bytes() for path in dependencies) + mappings.read_bytes()).hexdigest()
    stamp = folder / 'dependencies-named.txt'
    if not output.is_file() or not stamp.is_file() or stamp.read_text().strip() != signature:
        source = folder / 'dependencies-intermediary'
        if source.exists():
            if source.resolve().parent != folder.resolve(): raise ValueError('Invalid protocol dependency directory')
            shutil.rmtree(source)
        for path in dependencies: extract_classes(path, source)
        game = extract_classes(folder / 'intermediary.jar', folder / 'game-intermediary')
        subprocess.run([str(java), '-Xmx2G', '-cp', tools, 'ru.he3hauka.toolvisuals.protocols.build.SourceTools', 'remap', str(source), str(output), str(mappings), 'intermediary', 'named', 'plain', str(game)], check=True)
        stamp.write_text(signature + '\n')
    return output


def remap_sources(version, root, java, tools, classes, output, game, dependencies, standard):
    folder = root / 'build/components/viafabricplus/games' / version
    classpath = [extract_classes(game, folder / 'named-classpath/game'), extract_classes(dependencies, folder / 'named-classpath/dependencies')]
    standard_path = folder / 'named-classpath/standard'
    if not standard_path.exists():
        for path in standard: extract_classes(path, standard_path)
    classpath.append(standard_path)
    archive = output.parent / 'remapped-sources.jar'
    with (output.parent / 'remap.log').open('w', encoding='utf-8') as stream:
        result = subprocess.run([str(java), '-Xmx2G', '-cp', tools, 'ru.he3hauka.toolvisuals.protocols.build.SourceTools', 'remap', str(classes), str(archive), str(folder / 'named.tiny'), 'named', 'intermediary', 'mixins', *map(str, classpath)], stdout=stream, stderr=subprocess.STDOUT)
    if result.returncode: raise RuntimeError('Protocol Mixin remapping failed: ' + str(output.parent / 'remap.log'))
    if output.exists():
        if output.resolve().parent != classes.resolve().parent: raise ValueError('Invalid remapped protocol output directory')
        shutil.rmtree(output)
    return extract_classes(archive, output)


def extract_classes(archive, destination):
    destination.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(archive) as source:
        for name in source.namelist():
            if not name.endswith('.class') or name.startswith('META-INF/') or name == 'module-info.class': continue
            target = destination / name
            if not target.resolve().is_relative_to(destination.resolve()): raise ValueError('Invalid dependency archive path')
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(source.read(name))
    return destination


def library_bytes(path, metadata):
    data = path.read_bytes()
    with zipfile.ZipFile(io.BytesIO(data)) as original:
        if 'fabric.mod.json' in original.namelist(): return data
        files = {name: original.read(name) for name in original.namelist() if not name.endswith('/') and not re.fullmatch(r'META-INF/[^/]+\.(SF|RSA|DSA|EC)', name, re.I)}
    files['fabric.mod.json'] = json.dumps(metadata, indent=2).encode()
    return write_archive(files)


def source_manifest(version):
    namespace = 'official' if version == '26.3' else 'intermediary'
    remap = 'false' if version == '26.3' else 'true'
    return ('Manifest-Version: 1.0\r\nFabric-Mapping-Namespace: ' + namespace + '\r\nFabric-Loom-Remap: ' + remap + '\r\nFabric-Loom-Mixin-Remap-Type: static\r\n\r\n').encode()


def resources(folder, manifest):
    result = {}
    for path in folder.rglob('*'):
        if not path.is_file(): continue
        if path.suffix in {'.class', '.jar'}: raise ValueError('Protocol resources must not contain compiled code: ' + str(path))
        data = path.read_bytes()
        if b'\0' not in data: data = data.replace(b'\r\n', b'\n')
        result[path.relative_to(folder).as_posix()] = data
    result['META-INF/MANIFEST.MF'] = manifest
    metadata = json.loads(result['fabric.mod.json'])
    for entry in metadata.get('mixins', []):
        name = entry if isinstance(entry, str) else entry['config']
        config = json.loads(result[name])
        refmap = config.pop('refmap', None)
        if refmap: result.pop(refmap, None)
        result[name] = json.dumps(config, indent=2, ensure_ascii=False).encode('utf-8')
    return result


def build(version, root):
    from protocol_fork import artifact, directory, java_tools, manifest, source_digest
    java, javac = java_tools()
    fingerprint = source_digest(root)
    acquire(version, root)
    component = root / 'components/viafabricplus'
    output = directory(version, root)
    output.mkdir(parents=True, exist_ok=True)
    tools = prepare_tools(version, root, java, javac)
    locks = read_json(component / 'libraries.json')
    game = read_json(component / 'game-inputs.json')['targets'][version]
    game_libraries = [root / 'build/components/viafabricplus/games/libraries' / row['path'] for row in game['libraries']]
    game_jar = prepare_game(version, root, java, tools, game_libraries)
    dependencies = [root / 'build/components/viafabricplus/libraries' / name for name in locks['targets'][version]]
    classes = output / ('source-classes' if version == '26.3' else 'named-classes')
    if classes.exists():
        if classes.resolve().parent != output.resolve(): raise ValueError('Invalid protocol compiler output directory')
        shutil.rmtree(classes)
    source_root = component / 'src/targets' / version
    shared = sorted((component / 'src/shared/java').rglob('*.java'))
    modules = {folder.name: sorted((folder / 'java').rglob('*.java')) for folder in source_root.iterdir() if folder.is_dir()}
    sources = [*shared, *[path for group in modules.values() for path in group]]
    compile_dependencies = dependencies if version == '26.3' else [named_dependencies(version, root, java, tools, dependencies)]
    run_compiler(java, tools, output / 'compile.txt', sources, classes, [game_jar, *compile_dependencies, *game_libraries, *tool_classpath(version, root)], 25 if version == '26.3' else 21, output / 'compile.log')
    if version != '26.3': classes = remap_sources(version, root, java, tools, classes, output / 'source-classes', game_jar, compile_dependencies[0], game_libraries)
    owners = {}
    for module, paths in modules.items():
        for path in paths:
            package = re.search(r'\bpackage\s+([\w.]+)\s*;', path.read_text(encoding='utf-8'))
            if package: owners[package.group(1).replace('.', '/') + '/' + path.stem] = module
    for path in shared:
        package = re.search(r'\bpackage\s+([\w.]+)\s*;', path.read_text(encoding='utf-8')).group(1)
        owners[package.replace('.', '/') + '/' + path.stem] = 'main'
    header = source_manifest(version)
    contents = {module: resources(source_root / module / 'resources', header) for module in modules}
    flattened = {}
    for path in sorted(classes.rglob('*.class')):
        name = path.relative_to(classes).as_posix()
        owner = owners.get(name[:-6].split('$')[0])
        if owner is None: raise ValueError('Compiled class has no source owner: ' + name)
        contents[owner][name] = path.read_bytes(); flattened[name] = path.read_bytes()
    profile = manifest(root)['targets'][version]
    revision = manifest(root)['revision']
    main = contents['main']
    nested = {}
    for module, files in contents.items():
        if module != 'main': nested['META-INF/jars/toolvisuals-protocols-' + module + '-' + version + '.jar'] = write_archive(files)
    for name in locks['targets'][version]:
        row = locks['libraries'][name]
        data = library_bytes(root / 'build/components/viafabricplus/libraries' / name, row['metadata'])
        nested['META-INF/jars/' + name] = data
        with zipfile.ZipFile(io.BytesIO(data)) as library:
            for entry in library.namelist():
                if entry.endswith('.class') and not entry.startswith('META-INF/') and entry != 'module-info.class': flattened.setdefault(entry, library.read(entry))
    metadata = json.loads(main['fabric.mod.json'])
    metadata['name'] = 'ToolVisuals Protocols (ViaFabricPlus source fork)'
    metadata['version'] = profile['version'] + '+toolvisuals.' + str(revision)
    metadata['description'] = 'ToolVisuals Java protocols: 1.7.2 through ' + version + '. Built from the sources included with ToolVisuals.'
    metadata.setdefault('depends', {})['toolvisuals'] = '>=1.4'
    metadata['jars'] = [{'file': name} for name in sorted(nested)]
    flattened['META-INF/MANIFEST.MF'] = header
    compile_api = write_archive(flattened)
    provenance = {'schema': 2, 'revision': revision, 'source_tree_sha256': fingerprint, 'minecraft': version,
                  'minimum_java_protocol': '1.7.2', 'build_from_source': True, 'upstream': profile,
                  'source_files': len(sources), 'compiled_classes': len(list(classes.rglob('*.class'))),
                  'compile_api_sha512': hashlib.sha512(compile_api).hexdigest(),
                  'libraries_sha256': hashlib.sha256((component / 'libraries.json').read_bytes().replace(b'\r\n', b'\n')).hexdigest()}
    metadata.setdefault('custom', {})['toolvisuals:protocol-fork'] = provenance
    main['fabric.mod.json'] = json.dumps(metadata, indent=2, ensure_ascii=False).encode('utf-8')
    main['META-INF/toolvisuals/protocol-component.json'] = json.dumps(provenance, indent=2).encode()
    for notice in ['NOTICE', 'COPYING', 'ATTRIBUTION']: main['META-INF/toolvisuals/protocols-' + notice] = (component / notice).read_bytes().replace(b'\r\n', b'\n')
    main.update(nested)
    if source_digest(root) != fingerprint: raise RuntimeError('Protocol sources changed during compilation; repeat the build')
    write_changed(output / 'compile-api.jar', compile_api)
    write_changed(artifact(version, root), write_archive(main))
    print('PROTOCOL_SOURCE_OK', version, 'sources=' + str(len(sources)), 'classes=' + str(provenance['compiled_classes']), 'bytes=' + str(artifact(version, root).stat().st_size), flush=True)
    return artifact(version, root)
