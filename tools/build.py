"""Build the Pointers extension and the five-class TenacityGUI compatibility patch."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
from zipfile import ZipFile, ZIP_DEFLATED

ROOT = Path(__file__).resolve().parent.parent


def compile_sources(source, output, classpath):
    output.mkdir(parents=True, exist_ok=True)
    files = sorted(source.rglob('*.java'))
    arguments = output.parent / (output.name + '-sources.txt')
    arguments.write_text('\n'.join('"' + p.as_posix() + '"' for p in files), encoding='utf-8')
    subprocess.run(['javac', '--release', '8', '-proc:none', '-encoding', 'UTF-8',
                    '-classpath', classpath, '-d', str(output), '@' + str(arguments)], check=True)


def package(classes, output, entries):
    for path in sorted(classes.rglob('*.class')):
        entries[path.relative_to(classes).as_posix()] = path.read_bytes()
    output.parent.mkdir(parents=True, exist_ok=True)
    with ZipFile(output, 'w', ZIP_DEFLATED) as archive:
        for name in sorted(entries, key=lambda n: (n != 'META-INF/MANIFEST.MF', n)):
            archive.writestr(name, entries[name])
    with ZipFile(output) as archive:
        if archive.testzip() is not None:
            raise RuntimeError('Invalid extension archive: ' + str(output))
    print(output.relative_to(ROOT))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('--classpath', help='Meowtils plus Minecraft/Forge development libraries')
    group.add_argument('--classpath-file', type=Path, help='File containing the Java classpath')
    parser.add_argument('--tenacity-base', type=Path,
                        default=ROOT / 'releases/TenacityGUI-1.2.4.meowtils')
    args = parser.parse_args()
    if shutil.which('javac') is None:
        parser.error('A JDK with javac must be on PATH (JDK 17+ recommended).')
    classpath = args.classpath or args.classpath_file.read_text(encoding='utf-8-sig').strip()
    base = args.tenacity_base.resolve()
    if not base.is_file():
        parser.error('TenacityGUI base extension does not exist: ' + str(base))
    classpath += os.pathsep + str(base)
    pointers_classes = ROOT / 'build/pointers-classes'
    tenacity_classes = ROOT / 'build/tenacity-classes'
    compile_sources(ROOT / 'src/pointers', pointers_classes, classpath)
    compile_sources(ROOT / 'src/tenacity-compat', tenacity_classes, classpath)
    resources = ROOT / 'resources/pointers'
    entries = {p.relative_to(resources).as_posix(): p.read_bytes()
               for p in resources.rglob('*') if p.is_file()}
    names = set(entries) | {p.relative_to(pointers_classes).as_posix()
                            for p in pointers_classes.rglob('*.class')}
    entries['pointers-files.txt'] = ('\n'.join(sorted((names - {'META-INF/MANIFEST.MF'}) |
                                                  {'pointers-files.txt'})) + '\n').encode('utf-8')
    package(pointers_classes, ROOT / 'build/dist/PointersExtension-1.29.meowtils', entries)
    with ZipFile(base) as archive:
        entries = {n: archive.read(n) for n in archive.namelist() if not n.endswith('/')}
    package(tenacity_classes, ROOT / 'build/dist/TenacityGUI-1.2.4.meowtils', entries)


if __name__ == '__main__':
    main()
