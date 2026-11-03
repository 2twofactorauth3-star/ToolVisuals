from pathlib import Path
import hashlib
import io
import json
import shutil
import zipfile
from project import ROOT,OUT
from distribution import VERSION,pinned_music
from protocol_fork import resolve_rows,archive_for

def mod_versions(archive):
    with zipfile.ZipFile(archive) as source:
        metadata=json.loads(source.read('fabric.mod.json'))
        versions={metadata['id']:metadata['version']}
        for child in metadata.get('jars',[]):versions.update(mod_versions(io.BytesIO(source.read(child['file']))))
        return versions

def reset(path):
    resolved=path.resolve()
    if resolved.parent.parent!=OUT.resolve():raise RuntimeError('Output must remain within a version build directory')
    if path.exists():shutil.rmtree(path)
    path.mkdir(parents=True)

def resources(version):
    directory=OUT/version
    destination=directory/'resources'
    reset(destination)
    for source in [ROOT/'src/main/resources',ROOT/'versions/common/main/resources',ROOT/'versions'/version/'main/resources']:
        if not source.exists():continue
        for path in source.rglob('*'):
            if not path.is_file():continue
            relative=path.relative_to(source)
            if source==ROOT/'src/main/resources' and relative.as_posix().startswith('assets/sodium/shaders/'):continue
            target=destination/relative;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(path.read_bytes())
    rows=resolve_rows(version,json.loads((ROOT/'versions'/version/'dependencies.json').read_text()))
    mods={row['project']:row for row in rows}
    actual={project:mod_versions(archive_for(version,row)) for project,row in mods.items()}
    metadata_path=destination/'fabric.mod.json'
    metadata=json.loads(metadata_path.read_text(encoding='utf-8'))
    metadata['version']=VERSION
    target=next(target for target in json.loads((ROOT/'versions/targets.json').read_text(encoding='utf-8'))['targets'] if target['minecraft']==version)
    metadata['depends'].update({'fabricloader':'>='+target['fabric_loader'],'fabric-api':'>='+actual['fabric-api']['fabric-api'],'minecraft':version,'java':'>=21','viafabricplus':actual['viafabricplus']['viafabricplus'],'voicechat':actual['simple-voice-chat']['voicechat'],'voicechat_api':'>='+actual['simple-voice-chat']['voicechat_api'],'appleskin':actual['appleskin']['appleskin']})
    metadata['jars']=[{'file':'META-INF/jars/'+row['file']} for row in rows]
    metadata_path.write_text(json.dumps(metadata,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    from sources import contents
    available={path.with_suffix('').as_posix() for path in contents(version)}
    registered=set()
    for config in destination.glob('*.mixins.json'):
        data=json.loads(config.read_text(encoding='utf-8'));data['compatibilityLevel']='JAVA_21';prefix=data['package'].replace('.','/')+'/'
        for side in ['client','mixins','server']:
            if side in data:
                data[side]=[name for name in data[side] if prefix+name.replace('.','/') in available]
                registered.update(prefix+name.replace('.','/') for name in data[side])
        config.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
    config=destination/'toolvisuals.mixins.json';data=json.loads(config.read_text());prefix=data['package'].replace('.','/')+'/'
    for name in sorted(available):
        if name.startswith(prefix) and name not in registered:data['client'].append(name[len(prefix):].replace('/','.'))
    config.write_text(json.dumps(data,indent=2)+'\n')
    manifest=destination/'META-INF/toolvisuals';manifest.mkdir(parents=True,exist_ok=True)
    optimization=[]
    for row in rows:
        path=archive_for(version,row);raw=path.read_bytes()
        if len(raw)!=row['bytes'] or hashlib.sha512(raw).hexdigest()!=row['sha512']:raise RuntimeError('Invalid pinned archive: '+path.name)
        with zipfile.ZipFile(path) as source:identity=json.loads(source.read('fabric.mod.json'))
        if row['project'] not in {'fabric-api','viafabricplus','simple-voice-chat','appleskin','cloth-config','imagecodecs'}:optimization.append(dict(row,id=identity['id'],preserve_archive=False))
        if row['project'] in {'simple-voice-chat','appleskin'}:
            label='voicechat' if row['project']=='simple-voice-chat' else 'appleskin'
            (manifest/('bundled-'+label+'.json')).write_text(json.dumps(row,indent=2)+'\n')
    (manifest/'bundled-performance.json').write_text(json.dumps({'schema':1,'minecraft':version,'mods':optimization},indent=2)+'\n')
    (manifest/'native-target.json').write_text(json.dumps({'minecraft':version,'java':21,'mappings':'intermediary','version':VERSION},indent=2)+'\n')
    for path in pinned_music(ROOT):
        target=manifest/'music'/path.name;target.parent.mkdir(exist_ok=True);shutil.copyfile(path,target)
    return destination,rows

