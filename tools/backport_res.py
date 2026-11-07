#!/usr/bin/env python3
"""Copy resources from upstream main (26.3 formats) into this port (1.21.1 formats).

usage: tools/backport_res.py REGEX [REGEX...] [--dry] [--overwrite]
Each REGEX is searched against the path below the resource root, for example
    assets/nekomasfixed/textures/block/kiln.*  or  (^|/)kiln[^/]*\\.json$
Files already in this port are left alone unless --overwrite is given. Lang files are never copied.

Conversions:
  recipes        string ingredients to {"item"}/{"tag"}, result "id" kept, 26.x-only fields reported
  items/*.json   26.x item definitions become models/item/*.json when they are plain models;
                 anything else (tints, conditions, selects) is printed as MANUAL
  advancements   string icon/ingredient forms are normalised; everything else copied and reported if it
                 names something 1.21.1 lacks
Everything else is copied as is. Files naming content 1.21.1 lacks are printed as CHECK.
"""
import json
import os
import re
import shutil
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MAIN = os.environ.get('NEKOMA_MAIN', 'D:/Ports/nekomas-main/src/main')
SOURCES = [os.path.join(MAIN, 'resources'), os.path.join(MAIN, 'generated')]
DEST = os.path.join(ROOT, 'src', 'main', 'resources')
PORT_ROOTS = [DEST, os.path.join(ROOT, 'src', 'main', 'generated')]
MISSING_VANILLA = re.compile(
    r'minecraft:(pale_|resin|poplar|sulfur|copper_(?:helmet|chestplate|leggings|boots|sword|axe|pickaxe|shovel|hoe|chest|golem|bars|lantern|chain|torch)'
    r'|bundle|\w+_bundle|\w+_harness|happy_ghast|\w*spear|nautilus|zombie_nautilus|mace_never|\w*eyeblossom|\w+_shelf|lodestone_never|\w*dried_ghast|\w*cushion)')


def ingredient(value):
    if isinstance(value, str):
        return {'tag': value[1:]} if value.startswith('#') else {'item': value}
    if isinstance(value, list):
        return [ingredient(v) for v in value]
    return value


def convert_recipe(data):
    if 'ingredient' in data:
        data['ingredient'] = ingredient(data['ingredient'])
    if 'ingredients' in data:
        data['ingredients'] = [ingredient(v) for v in data['ingredients']]
    if isinstance(data.get('key'), dict):
        data['key'] = {k: ingredient(v) for k, v in data['key'].items()}
    for field in ('base', 'addition', 'template'):
        if field in data:
            data[field] = ingredient(data[field])
    data.pop('show_notification', None) if data.get('show_notification') is True else None
    return data


def main():
    args = sys.argv[1:]
    dry = '--dry' in args
    overwrite = '--overwrite' in args
    patterns = [re.compile(a) for a in args if not a.startswith('--')]
    if not patterns:
        print(__doc__)
        return 2
    copied = skipped = 0
    manual = []
    check = []
    for source in SOURCES:
        for directory, dirs, files in os.walk(source):
            dirs[:] = [d for d in dirs if d != '.cache']
            for name in files:
                full = os.path.join(directory, name)
                rel = os.path.relpath(full, source).replace('\\', '/')
                if not any(p.search(rel) for p in patterns) or '/lang/' in rel:
                    continue
                target_rel = rel
                payload = None
                if rel.endswith('.json'):
                    try:
                        data = json.load(open(full, encoding='utf8'))
                    except ValueError:
                        data = None
                    if data is not None:
                        m = re.match(r'assets/([^/]+)/items/(.+)\.json$', rel)
                        if m:
                            model = data.get('model', {})
                            if model.get('type', 'minecraft:model') != 'minecraft:model' or 'tints' in model:
                                manual.append(rel)
                                continue
                            ref = model.get('model', '')
                            if ref == f'{m.group(1)}:item/{m.group(2)}':
                                # the same-named models/item file carries it, nothing to write for this one
                                continue
                            target_rel = f'assets/{m.group(1)}/models/item/{m.group(2)}.json'
                            data = {'parent': ref}
                        elif re.match(r'data/[^/]+/recipe/', rel):
                            data = convert_recipe(data)
                        text = json.dumps(data, indent=2, ensure_ascii=False)
                        if MISSING_VANILLA.search(text):
                            check.append(rel)
                        payload = text + '\n'
                exists = any(os.path.exists(os.path.join(r, target_rel)) for r in PORT_ROOTS)
                if exists and not overwrite:
                    skipped += 1
                    continue
                print(('would copy ' if dry else 'copy ') + target_rel)
                copied += 1
                if dry:
                    continue
                out = os.path.join(DEST, target_rel)
                os.makedirs(os.path.dirname(out), exist_ok=True)
                if payload is not None:
                    with open(out, 'w', encoding='utf8', newline='\n') as handle:
                        handle.write(payload)
                else:
                    shutil.copyfile(full, out)
    print(f'{copied} copied, {skipped} already here')
    for item in manual:
        print('MANUAL item definition:', item)
    for item in check:
        print('CHECK names content 1.21.1 lacks:', item)
    return 0


if __name__ == '__main__':
    sys.exit(main())
