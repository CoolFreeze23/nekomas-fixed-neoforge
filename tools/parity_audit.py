#!/usr/bin/env python3
"""Compares this port's content with upstream main: registered ids, recipes, loot, advancements, tags, textures.

Prints what main has and this port lacks, leaving out what PORTING.md lists as left out on purpose.
"""
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MAIN = os.environ.get('NEKOMA_MAIN', os.path.join(ROOT, '..', 'nekomas-main'))
LEFT_OUT = re.compile(
    r'pale_oak|poplar|eyeblossom|cushion|harness|_bundle|(^|[/_:])spear|goat_horn_copper|copper_armor|_banner(_|$)|resin|shelf_never|'
    r'(amber|aqua|indigo|maroon)_banner|banner/(amber|aqua|indigo|maroon)')


def tree(base):
    found = {}
    for root in ('src/main/resources', 'src/main/generated'):
        full = os.path.join(base, root)
        for directory, dirs, files in os.walk(full):
            dirs[:] = [d for d in dirs if d != '.cache']
            for name in files:
                rel = os.path.relpath(os.path.join(directory, name), full).replace('\\', '/')
                found[rel] = os.path.join(directory, name)
    return found


def ids(base, sources):
    """Every id passed to a register-like call in the Java sources."""
    out = set()
    for source in sources:
        full = os.path.join(base, source)
        for directory, _, files in os.walk(full):
            for name in files:
                if not name.endswith('.java'):
                    continue
                text = open(os.path.join(directory, name), encoding='utf8', errors='ignore').read()
                if 'registry/registries' not in directory.replace('\\', '/'):
                    continue
                out.update(re.findall(r'(?:register\w*|block|item|entity|boat|id)\(\s*"([a-z0-9_/]+)"', text))
    return out


mine = tree(ROOT)
theirs = tree(MAIN)

groups = {}
for rel, path in theirs.items():
    if rel in mine or LEFT_OUT.search(rel) or '/lang/' in rel:
        continue
    m = re.match(r'(assets|data)/([^/]+)/([^/]+)(?:/([^/]+))?', rel)
    kind = '/'.join(p for p in (m.group(1), m.group(3), m.group(4) if m.group(3) in ('tags', 'worldgen', 'textures', 'models') else None) if p) if m else rel
    groups.setdefault(kind, []).append(rel)

skip = re.compile(r'^assets/items$|^assets/equipment$|^data/villager_trade|^data/trade_set|^data/tags/villager_trade')
print('=== files on main with no counterpart here')
for kind in sorted(groups):
    names = [os.path.splitext(os.path.basename(p))[0] for p in groups[kind]]
    if skip.match(kind):
        print(f'{kind}: {len(names)} (different system on 1.21.1)')
        continue
    print(f'{kind}: {len(names)}\n   ' + ' '.join(sorted(names))[:1800])

main_ids = ids(MAIN, ['src/main/java'])
port_ids = ids(ROOT, ['src/main/java'])
missing = sorted(i for i in main_ids - port_ids if not LEFT_OUT.search(i))
print('\n=== ids registered on main, not here\n' + ' '.join(missing))

print('\n=== en_us keys on main, not here')
en_main = json.load(open(os.path.join(MAIN, 'src/main/resources/assets/nekomasfixed/lang/en_us.json'), encoding='utf8'))
en_port = json.load(open(os.path.join(ROOT, 'src/main/resources/assets/nekomasfixed/lang/en_us.json'), encoding='utf8'))
print(' '.join(sorted(k for k in en_main if k not in en_port and not LEFT_OUT.search(k))))
