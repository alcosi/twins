#!/usr/bin/env python3
"""
Renumber permission i18n UUIDs from random to deterministic range 0012-000000001000..002000.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA_FILE = ROOT / 'core/src/main/java/org/twins/bootstrap/SystemEntityBootstrapData.java'
IDS_FILE = ROOT / 'core/src/main/java/org/twins/core/enums/consts/SystemIds.java'
MIGRATION_FILE = ROOT / 'core/src/main/resources/db/migration_backlog/V1.4.290.01__permission_i18n_renumber.sql'

I18N_TYPE_NAME = 'permissionName'
I18N_TYPE_DESC = 'permissionDescription'


def parse_permission_uuid_map(system_ids_content):
    """Flatten sub-holders: 'Twinflow.DELETE' -> uuid, 'PERMISSION_GROUP_DEFAULT' -> uuid."""
    mapping = {}
    top_start = system_ids_content.find('public static final class Permission {')
    if top_start < 0:
        raise RuntimeError('No Permission holder')
    depth = 0
    top_end = -1
    for i in range(top_start, len(system_ids_content)):
        if system_ids_content[i] == '{':
            depth += 1
        elif system_ids_content[i] == '}':
            depth -= 1
            if depth == 0:
                top_end = i + 1
                break
    section = system_ids_content[top_start:top_end]
    pat_uuid = re.compile(r'public\s+static\s+final\s+UUID\s+(\w+)\s*=\s*UUID\.fromString\("([0-9a-fA-F-]+)"\)')

    first_sub = section.find('public static final class ', 1)
    top_part = section if first_sub < 0 else section[:first_sub]
    for m in pat_uuid.finditer(top_part):
        mapping[m.group(1)] = m.group(2)

    sub_start = first_sub
    while sub_start >= 0 and sub_start < len(section):
        m_class = re.match(r'public\s+static\s+final\s+class\s+(\w+)\s*\{', section[sub_start:])
        if not m_class:
            break
        sub_name = m_class.group(1)
        body_start = sub_start + m_class.end()
        depth = 1
        body_end = -1
        for j in range(body_start, len(section)):
            if section[j] == '{':
                depth += 1
            elif section[j] == '}':
                depth -= 1
                if depth == 0:
                    body_end = j
                    break
        body = section[body_start:body_end]
        for m in pat_uuid.finditer(body):
            mapping[f'{sub_name}.{m.group(1)}'] = m.group(2)
        next_search = body_end + 1
        next_sub = section.find('public static final class ', next_search)
        sub_start = next_sub if next_sub >= 0 else -1
    return mapping


def find_permission_blocks(content):
    """Return list of (start_char, end_char) for each top-level SystemPermission(...) call."""
    blocks = []
    search_from = 0
    while True:
        idx = content.find('new SystemPermission(', search_from)
        if idx < 0:
            break
        # Walk forward from idx, tracking paren depth. The opening '(' is right after 'SystemPermission'.
        open_paren = idx + len('new SystemPermission')
        depth = 1
        j = open_paren + 1
        while j < len(content) and depth > 0:
            c = content[j]
            if c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
            j += 1
        if depth != 0:
            raise RuntimeError(f'Unbalanced parens starting at offset {idx}')
        blocks.append((idx, j))  # j points just past the closing ')'
        search_from = j
    return blocks


def parse_permissions(content, perm_uuid_map):
    """Return list of {perm_id_const, perm_uuid, key_name, name_i18n_uuid, name_translation,
    desc_i18n_uuid, desc_translation, start_char, end_char}."""
    blocks = find_permission_blocks(content)
    parsed = []
    uuid_re = re.compile(r'new I18n\(UUID\.fromString\("([0-9a-fA-F-]+)"\),\s*"((?:[^"\\]|\\.)*)"\)')
    id_const_re = re.compile(r'new SystemPermission\((SystemIds\.Permission\.[A-Za-z0-9._]+)')
    key_re = re.compile(r'Permissions\.([A-Z][A-Z0-9_]*)\.name\(\)')

    for start, end in blocks:
        block = content[start:end]
        id_m = id_const_re.search(block)
        key_m = key_re.search(block)
        i18n_blocks = uuid_re.findall(block)
        const_name = id_m.group(1) if id_m else None
        perm_uuid = None
        if const_name:
            suffix = const_name.split('SystemIds.Permission.')[1]
            perm_uuid = perm_uuid_map.get(suffix)
        parsed.append({
            'start_char': start,
            'end_char': end,
            'perm_id_const': const_name,
            'perm_uuid': perm_uuid,
            'key_name': key_m.group(1) if key_m else None,
            'name_i18n_uuid': i18n_blocks[0][0] if len(i18n_blocks) >= 1 else None,
            'name_translation': i18n_blocks[0][1] if len(i18n_blocks) >= 1 else None,
            'desc_i18n_uuid': i18n_blocks[1][0] if len(i18n_blocks) >= 2 else None,
            'desc_translation': i18n_blocks[1][1] if len(i18n_blocks) >= 2 else None,
        })
    return parsed


def make_translation_from_key(key_name):
    words = key_name.lower().split('_')
    if words and words[0] == 'twin':
        words = words[1:]
    if not words:
        return key_name
    return words[0].capitalize() + ''.join(' ' + w for w in words[1:])


def assign_uuids(parsed):
    counter = 0x1000
    for p in parsed:
        p['new_name_uuid'] = f'00000000-0000-0000-0012-{counter:012x}'
        p['new_desc_uuid'] = f'00000000-0000-0000-0012-{counter + 1:012x}'
        counter += 2


def esc(s):
    return s.replace("'", "''")


def build_migration(parsed):
    out = []
    out.append('-- Renumber permission i18n UUIDs from random to deterministic range.')
    out.append('-- All permission i18n entries (existing + newly added) get UUIDs in')
    out.append("-- '00000000-0000-0000-0012-000000001000'..'.002000' range.")
    out.append('-- Generated by scripts/renumber_permission_i18n.py')
    out.append('')

    out.append('-- Step 1: Renumber existing i18n IDs.')
    for p in parsed:
        if p['name_i18n_uuid']:
            out.append(f"UPDATE i18n SET id = '{p['new_name_uuid']}'::uuid WHERE id = '{p['name_i18n_uuid']}'::uuid;")
        if p['desc_i18n_uuid']:
            out.append(f"UPDATE i18n SET id = '{p['new_desc_uuid']}'::uuid WHERE id = '{p['desc_i18n_uuid']}'::uuid;")
    out.append('')

    out.append('-- Step 2: Insert missing i18n + translations (permissions that had null i18n).')
    insert_i18n = []
    insert_tr = []
    for p in parsed:
        if not p['name_i18n_uuid']:
            tr = p['name_translation'] or make_translation_from_key(p['key_name'])
            insert_i18n.append(f"('{p['new_name_uuid']}'::uuid, null, null, '{I18N_TYPE_NAME}'::varchar)")
            insert_tr.append(f"('{p['new_name_uuid']}'::uuid, 'en', '{esc(tr)}', 0)")
        if not p['desc_i18n_uuid']:
            tr = p['desc_translation'] or (p['name_translation'] or make_translation_from_key(p['key_name']))
            insert_i18n.append(f"('{p['new_desc_uuid']}'::uuid, null, null, '{I18N_TYPE_DESC}'::varchar)")
            insert_tr.append(f"('{p['new_desc_uuid']}'::uuid, 'en', '{esc(tr)}', 0)")
    if insert_i18n:
        out.append('INSERT INTO i18n (id, name, key, i18n_type_id) VALUES')
        out.append('    ' + ',\n    '.join(insert_i18n))
        out.append('ON CONFLICT (id) DO NOTHING;')
        out.append('')
        out.append('INSERT INTO i18n_translation (i18n_id, locale, translation, usage_counter) VALUES')
        out.append('    ' + ',\n    '.join(insert_tr))
        out.append('ON CONFLICT (i18n_id, locale) DO UPDATE SET translation = excluded.translation;')
        out.append('')

    out.append('-- Step 3: Update permission rows to point at new i18n IDs.')
    out.append('UPDATE permission AS p SET')
    out.append('    name_i18n_id = u.name_i18n_id,')
    out.append('    description_i18n_id = u.desc_i18n_id')
    out.append('FROM (VALUES')
    rows = []
    for p in parsed:
        rows.append(f"    ('{p['perm_uuid']}'::uuid, '{p['new_name_uuid']}'::uuid, '{p['new_desc_uuid']}'::uuid) -- {p['key_name']}")
    out.append(',\n'.join(rows))
    out.append(') AS u(id, name_i18n_id, desc_i18n_id)')
    out.append('WHERE p.id = u.id;')
    return '\n'.join(out) + '\n'


def update_bootstrap_data(parsed, content):
    """Rewrite each SystemPermission(...) block to use new UUIDs and never-null i18n.

    We split each block by top-level commas (depth == 1) into 4 pieces:
    [0] = 'new SystemPermission(<id>'
    [1] = '<key>'
    [2] = '<name i18n or null>'
    [3] = '<desc i18n or null>)'
    Then rebuild [2] and [3] with new UUIDs and explicit I18n wrappers.
    """
    out = []
    cursor = 0
    for p in parsed:
        out.append(content[cursor:p['start_char']])
        block = content[p['start_char']:p['end_char']]
        # Parse top-level pieces
        depth = 0
        pieces = []
        cur = ''
        for ch in block:
            if ch == '(':
                depth += 1
                cur += ch
            elif ch == ')':
                depth -= 1
                cur += ch
            elif ch == ',' and depth == 1:
                pieces.append(cur.strip())
                cur = ''
            else:
                cur += ch
        if cur:
            tail = cur.rstrip().rstrip(',')
            if tail.endswith(')'):
                tail = tail[:-1].rstrip()
            pieces.append(tail.strip())

        # pieces[0] = 'new SystemPermission(<id>'
        # pieces[1] = '<key>'
        # pieces[2] = '<name i18n or null>'
        # pieces[3] = '<desc i18n or null>'
        if len(pieces) != 4:
            raise RuntimeError(f'Expected 4 pieces, got {len(pieces)}: {pieces}')

        name_tr = p['name_translation'] or make_translation_from_key(p['key_name'])
        desc_tr = p['desc_translation'] or name_tr
        pieces[2] = f'new I18n(UUID.fromString("{p["new_name_uuid"]}"), "{name_tr}")'
        pieces[3] = f'new I18n(UUID.fromString("{p["new_desc_uuid"]}"), "{desc_tr}")'

        out.append(', '.join(pieces) + ')')
        cursor = p['end_char']
    out.append(content[cursor:])
    return ''.join(out)


def main():
    content = DATA_FILE.read_text(encoding='utf-8')
    ids_content = IDS_FILE.read_text(encoding='utf-8')

    perm_uuid_map = parse_permission_uuid_map(ids_content)
    print(f'Loaded {len(perm_uuid_map)} permission UUIDs from SystemIds.Permission')

    parsed = parse_permissions(content, perm_uuid_map)
    print(f'Parsed {len(parsed)} permissions from SystemEntityBootstrapData')

    missing = [p for p in parsed if not p['perm_uuid']]
    if missing:
        print(f'ERROR: {len(missing)} permissions missing perm_uuid. First: {missing[0]}')
        sys.exit(1)

    with_i18n = sum(1 for p in parsed if p['name_i18n_uuid'])
    print(f'  with existing i18n: {with_i18n}')
    print(f'  without i18n:        {len(parsed) - with_i18n}')

    assign_uuids(parsed)

    sql = build_migration(parsed)
    MIGRATION_FILE.parent.mkdir(parents=True, exist_ok=True)
    MIGRATION_FILE.write_text(sql, encoding='utf-8')
    print(f'Wrote migration: {MIGRATION_FILE} ({len(sql)} bytes)')

    new_content = update_bootstrap_data(parsed, content)
    DATA_FILE.write_text(new_content, encoding='utf-8')
    print(f'Updated {DATA_FILE}')


if __name__ == '__main__':
    main()
