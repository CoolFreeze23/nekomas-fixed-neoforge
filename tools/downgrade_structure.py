#!/usr/bin/env python3
"""Rewrites 26.x structure files for 1.21.1: palette id/properties become Name/Properties, DataVersion becomes 3955.

usage: tools/downgrade_structure.py FILE.nbt...
"""
import gzip
import struct
import sys

TARGET_VERSION = 3955  # Minecraft 1.21.1


def read(data, pos, kind):
    if kind == 1:
        return data[pos:pos + 1], pos + 1
    if kind == 2:
        return data[pos:pos + 2], pos + 2
    if kind in (3, 5):
        return data[pos:pos + 4], pos + 4
    if kind in (4, 6):
        return data[pos:pos + 8], pos + 8
    if kind == 7:
        size = struct.unpack('>i', data[pos:pos + 4])[0]
        return data[pos:pos + 4 + size], pos + 4 + size
    if kind == 8:
        size = struct.unpack('>H', data[pos:pos + 2])[0]
        return data[pos:pos + 2 + size], pos + 2 + size
    if kind == 9:
        inner = data[pos]
        size = struct.unpack('>i', data[pos + 1:pos + 5])[0]
        pos += 5
        items = []
        for _ in range(size):
            value, pos = read(data, pos, inner)
            items.append(value)
        return (inner, items), pos
    if kind == 10:
        entries = []
        while data[pos] != 0:
            tag = data[pos]
            pos += 1
            name, pos = read(data, pos, 8)
            value, pos = read(data, pos, tag)
            entries.append([tag, name.decode('utf8') if False else name, value])
        return entries, pos + 1
    if kind == 11:
        size = struct.unpack('>i', data[pos:pos + 4])[0]
        return data[pos:pos + 4 + 4 * size], pos + 4 + 4 * size
    if kind == 12:
        size = struct.unpack('>i', data[pos:pos + 4])[0]
        return data[pos:pos + 4 + 8 * size], pos + 4 + 8 * size
    raise ValueError(f'unknown tag {kind}')


def write(value, kind):
    if kind == 9:
        inner, items = value
        return bytes([inner]) + struct.pack('>i', len(items)) + b''.join(write(v, inner) for v in items)
    if kind == 10:
        return b''.join(bytes([tag]) + name + write(val, tag) for tag, name, val in value) + b'\0'
    return value


def text(value):
    return value[2:].decode('utf8')


def encode(value):
    raw = value.encode('utf8')
    return struct.pack('>H', len(raw)) + raw


def convert(path):
    data = gzip.open(path).read()
    root_kind = data[0]
    root_name, pos = read(data, 1, 8)
    root, _ = read(data, pos, root_kind)
    changed = 0
    for entry in root:
        name = text(entry[1])
        if name == 'DataVersion':
            entry[2] = struct.pack('>i', TARGET_VERSION)
        elif name == 'palette':
            for state in entry[2][1]:
                for field in state:
                    if text(field[1]) == 'id':
                        field[1] = encode('Name')
                        changed += 1
                    elif text(field[1]) == 'properties':
                        field[1] = encode('Properties')
    with gzip.open(path, 'wb') as handle:
        handle.write(bytes([root_kind]) + root_name + write(root, root_kind))
    print(f'{path}: {changed} palette entries')


for file in sys.argv[1:]:
    convert(file)
