from pathlib import Path
import os
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[2]

def build():
    environment = dict(os.environ)
    cargo = environment.get('CARGO') or shutil.which('cargo')
    if not cargo:
        raise RuntimeError('Install Rust 1.90 or newer with the x86_64-pc-windows-gnu toolchain')
    rustc = environment.get('RUSTC') or shutil.which('rustc')
    if not rustc:
        raise RuntimeError('Rust compiler is required')
    sysroot = Path(subprocess.check_output([rustc, '--print', 'sysroot'], env=environment, text=True).strip())
    linker = sysroot / 'lib/rustlib/x86_64-pc-windows-gnu/bin/self-contained/x86_64-w64-mingw32-gcc.exe'
    host = subprocess.check_output([rustc, '-vV'], env=environment, text=True)
    if 'host: x86_64-pc-windows-' not in host:
        raise RuntimeError('Use a Windows x64 Rust host toolchain')
    if 'host: x86_64-pc-windows-gnu' in host and linker.is_file() and 'CARGO_ENCODED_RUSTFLAGS' not in environment and 'RUSTFLAGS' not in environment:
        environment['CARGO_ENCODED_RUSTFLAGS'] = '-Clinker=' + str(linker)
    environment['CARGO_TARGET_DIR'] = str(ROOT / 'build/launcher-native')
    subprocess.run([cargo, 'build', '--locked', '--release', '--manifest-path', str(Path(__file__).with_name('Cargo.toml'))], cwd=ROOT, env=environment, check=True)
    source = ROOT / 'build/launcher-native/release/tool_launcher_core.dll'
    if not source.is_file():
        raise RuntimeError('The Rust build did not produce the Windows native library')
    shutil.copyfile(source, ROOT / 'build/launcher-native/tool_launcher_core.dll')

if __name__ == '__main__':
    build()
