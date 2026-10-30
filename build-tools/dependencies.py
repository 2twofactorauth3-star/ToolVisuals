from pathlib import Path
import argparse
import concurrent.futures
import hashlib
import json
import re
import tempfile
import urllib.request

ROOT=Path(__file__).resolve().parents[1]
TARGETS=('26.3','1.21.11','1.21.4')

def download(url,path,algorithm=None,expected=None,size=None):
    path=Path(path)
    if path.is_file() and (size is None or path.stat().st_size==size) and (expected is None or hashlib.new(algorithm,path.read_bytes()).hexdigest()==expected):return path
    if not url.startswith('https://'):raise ValueError('Dependency downloads require HTTPS')
    path.parent.mkdir(parents=True,exist_ok=True)
    request=urllib.request.Request(url,headers={'User-Agent':'ToolVisuals-build'})
    with urllib.request.urlopen(request,timeout=60) as response:data=response.read() if size is None else response.read(size+1)
    if size is not None and len(data)!=size:raise ValueError('Dependency size mismatch: '+path.name)
    if expected is not None and hashlib.new(algorithm,data).hexdigest()!=expected:raise ValueError('Dependency checksum mismatch: '+path.name)
    temporary=None
    try:
        with tempfile.NamedTemporaryFile(dir=path.parent,prefix=path.name+'.',suffix='.download',delete=False) as output:
            temporary=Path(output.name);output.write(data)
        temporary.replace(path)
    finally:
        if temporary is not None:temporary.unlink(missing_ok=True)
    return path

def entries(root,target):
    if target not in TARGETS:raise ValueError('Unsupported native Minecraft target: '+target)
    if target=='26.3':
        manifest=root/'libs/performance/bundle.json'
        bundle=json.loads(manifest.read_text(encoding='utf-8'))
        if bundle['minecraft']!=target:raise ValueError('Performance dependencies target a different Minecraft version')
        result=[(manifest.parent/row['file'],row) for row in bundle['mods']]
        for name in ['appleskin','voicechat']:
            manifest=root/'libs'/name/(name+'.json');row=json.loads(manifest.read_text(encoding='utf-8'));result.append((manifest.parent/row['file'],row))
        return result
    manifest=root/'versions'/target/'dependencies.json'
    return [(None if row.get('project')=='viafabricplus' else manifest.parent/'libs'/row['file'],row) for row in json.loads(manifest.read_text(encoding='utf-8'))]

def acquire(target,root=ROOT):
    rows=entries(root,target)
    def fetch(entry):
        path,row=entry
        if row.get('project')=='viafabricplus':return row
        if path.name!=row['file'] or '/' in row['file'] or '\\' in row['file'] or not row['file'].endswith('.jar'):raise ValueError('Invalid dependency filename')
        if not re.fullmatch('[0-9a-f]{128}',row['sha512']):raise ValueError('Invalid pinned SHA-512: '+path.name)
        download(row.get('url') or row.get('download') or row['source'],path,'sha512',row['sha512'],row['bytes'])
        return row
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:result=list(pool.map(fetch,rows))
    from protocol_fork import build_component,resolve_rows
    build_component(target, root)
    print('DEPENDENCIES_READY',target,len(result),flush=True)
    return resolve_rows(target,result,root)

if __name__=='__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('--target',choices=TARGETS)
    args=parser.parse_args()
    for target in [args.target] if args.target else TARGETS:acquire(target)
