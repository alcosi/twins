import { execFileSync } from "node:child_process";
import { writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const OUT = dirname(fileURLToPath(import.meta.url));
const BASE = "http://127.0.0.1:8080";
const HEADERS = {
  DomainId: "f67ad556-dd27-4871-9a00-16fb0e8a4102",
  AuthToken: "608c6d7d-99c8-4d87-89c6-2f72d0f5d673,9a3f6075-f175-41cd-a804-934201ec969c",
  Channel: "WEB",
  "Content-Type": "application/json",
  Accept: "application/json",
};
const TASK = "7c027b60-0f6c-445c-9889-8ee3855d2c59";
const ASSIGNEE = "00000000-0000-0000-0011-000000000007";
const STATUS = "00000000-0000-0000-0011-000000000010";
const NAME = "00000000-0000-0000-0011-000000000003";
const CREATED = "00000000-0000-0000-0011-000000000011";
const WORK = "95f2c8ca-8b79-4971-9726-270a11c7d031";
const BOOL = "19e3f226-5249-46eb-8d01-e9a054494715";
const DECIMAL = "e454db8e-c8d8-48cb-9684-00abef38ee9e";
const USER_FIELD = "27af06b8-f8c9-4603-bc40-f4f6259630c0";
const MILESTONE = "6ecf4b17-791b-43d7-aaab-cfa9185fe6df";
const SAFETY = "667d5d27-e2e2-4e07-b2fa-9d3285de259d";
const LINK_M = "19cc54fb-06f1-4cb4-8628-f53f252a1024";
const LINK_S = "b684473a-2f90-45a3-be4d-b3787439bf24";
const STATUS_MID = "c432b42e-0dfe-4c9d-8c21-5a54aa7b1257";
const SHOW = [
  "showTwinMode=SHORT",
  "showTwin2UserMode=SHORT",
  "showTwin2StatusMode=SHORT",
  "showDataListOptionMode=SHORT",
  "showTwinField2DataListOptionMode=SHORT",
  "lazyRelation=false",
].join("&");

const checks = [];
function add(name, ok, detail) {
  checks.push({ name, ok: Boolean(ok), detail });
  const text = detail == null ? "" : " — " + clip(detail);
  console.log((ok ? "PASS " : "FAIL ") + name + text);
}
function clip(value) {
  const text = typeof value === "string" ? value : JSON.stringify(value);
  return text.length > 500 ? text.slice(0, 500) + "…" : text;
}
function psql(sql) {
  return execFileSync("docker", ["exec", "-i", "7bd7c8f7c221", "psql", "-U", "postgres", "-d", "wnr", "-v", "ON_ERROR_STOP=1", "-q", "-t", "-A", "-c", sql], { encoding: "utf8", maxBuffer: 32 * 1024 * 1024 }).trim();
}
function psqlJson(sql) {
  const text = psql(sql);
  return text ? JSON.parse(text) : null;
}
async function api(body, query = SHOW, timeoutMs = 180000) {
  const started = Date.now();
  const response = await fetch(BASE + "/private/twin/search/grouped/v1?" + query, {
    method: "POST",
    headers: HEADERS,
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(timeoutMs),
  });
  const text = await response.text();
  let parsed;
  try { parsed = text ? JSON.parse(text) : {}; } catch { parsed = { raw: text.slice(0, 400) }; }
  parsed.__http = response.status;
  parsed.__ms = Date.now() - started;
  console.log(`  ${response.status} ${parsed.__ms}ms groups=${parsed.groups?.length ?? "-"} status=${parsed.status}`);
  return parsed;
}
function body(levels, extra = {}, sorts) {
  const request = { search: { twinClassIdList: [TASK], ...extra }, levels };
  if (sorts) request.memberSorts = sorts;
  return request;
}
function field(id, sort) {
  const item = { fieldId: id };
  if (sort) item.sort = sort;
  return item;
}

const SIGN = 1n << 63n;
function uuidCompare(left, right) {
  const halves = (value) => {
    const raw = value.replace(/-/g, "");
    let hi = BigInt("0x" + raw.slice(0, 16));
    let lo = BigInt("0x" + raw.slice(16));
    if (hi >= SIGN) hi -= SIGN << 1n;
    if (lo >= SIGN) lo -= SIGN << 1n;
    return [hi, lo];
  };
  const [lhi, llo] = halves(left);
  const [rhi, rlo] = halves(right);
  if (lhi !== rhi) return lhi < rhi ? -1 : 1;
  if (llo !== rlo) return llo < rlo ? -1 : 1;
  return 0;
}
function nullsLast(left, right) {
  if (left === right) return 0;
  if (left == null) return 1;
  if (right == null) return -1;
  return 0;
}
function compareText(left, right, ascending) {
  if (left == null || right == null) return nullsLast(left, right);
  const compared = left < right ? -1 : left > right ? 1 : 0;
  return ascending ? compared : -compared;
}
function byKey(ascending) {
  return (a, b) => {
    if (a.key == null || b.key == null) return nullsLast(a.key, b.key);
    const compared = typeof a.key === "string" && typeof b.key === "string" ? uuidCompare(a.key, b.key) : compareText(String(a.key), String(b.key), true);
    return ascending ? compared : -compared;
  };
}
function byName(names, ascending) {
  return (a, b) => {
    if (a.key == null || b.key == null) return nullsLast(a.key, b.key);
    const compared = compareText(names.get(a.key) ?? null, names.get(b.key) ?? null, ascending);
    if (compared !== 0) return compared;
    return uuidCompare(a.key, b.key);
  };
}
function byCount(ascending) {
  return (a, b) => {
    let compared = a.count - b.count;
    if (!ascending) compared = -compared;
    if (compared !== 0) return compared;
    return byKey(true)(a, b);
  };
}
function build(twins, levelsOf, sorters) {
  const root = { children: new Map(), ids: new Set() };
  const seen = new Set();
  for (const twin of twins) {
    if (!seen.add(twin.id)) continue;
    expand(root, twin, levelsOf(twin), 0);
  }
  return list(root, 0, sorters);
}
function expand(node, twin, levels, depth) {
  if (depth === levels.length) {
    if (!node.ids.has(twin.id)) {
      node.ids.add(twin.id);
      node.order.push(twin.id);
    }
    return;
  }
  node.ids.add(twin.id);
  let keys = levels[depth];
  if (!keys || !keys.length) keys = [null];
  const used = new Set();
  for (const key of keys) {
    const token = key == null ? "\0" : JSON.stringify(key);
    if (used.has(token)) continue;
    used.add(token);
    let child = node.children.get(token);
    if (!child) {
      child = { key: key ?? null, children: new Map(), ids: new Set(), order: [] };
      node.children.set(token, child);
    }
    expand(child, twin, levels, depth + 1);
  }
}
function list(node, level, sorters) {
  const groups = [...node.children.values()];
  for (const group of groups) group.count = group.ids.size;
  groups.sort(sorters[level]);
  const last = level === sorters.length - 1;
  return groups.map((group) => ({
    key: group.key,
    count: group.count,
    groups: last ? undefined : list(group, level + 1, sorters),
    ids: last ? group.order : undefined,
  }));
}
function norm(value) {
  if (value == null) return null;
  if (typeof value === "string" && /^[0-9a-f-]{36}$/i.test(value)) return value.toLowerCase();
  return value;
}
function actual(groups) {
  return (groups || []).map((group) => ({
    type: group.key?.type,
    key: norm(group.key?.value),
    valuePresent: group.key != null && Object.prototype.hasOwnProperty.call(group.key, "value"),
    count: Number(group.count),
    hasGroups: Object.prototype.hasOwnProperty.call(group, "groups"),
    hasIds: Object.prototype.hasOwnProperty.call(group, "twinIdList"),
    hasTwins: Object.prototype.hasOwnProperty.call(group, "twins"),
    groups: group.groups ? actual(group.groups) : undefined,
    ids: group.twinIdList || [],
  }));
}
function compareTree(got, want, problems, path, types) {
  if (got.length !== want.length) {
    problems.push(`${path} groups ${got.length} != ${want.length}`);
    return;
  }
  for (let i = 0; i < want.length; i++) {
    const here = `${path}[${i}]`;
    if (got[i].type !== types[0]) problems.push(`${here} type ${got[i].type} != ${types[0]}`);
    if (!got[i].valuePresent) problems.push(`${here} value property missing`);
    if (JSON.stringify(got[i].key) !== JSON.stringify(want[i].key)) {
      problems.push(`${here} ${JSON.stringify(got[i].key)} != ${JSON.stringify(want[i].key)}`);
      continue;
    }
    if (got[i].count !== want[i].count) problems.push(`${here} count ${got[i].count} != ${want[i].count}`);
    if (want[i].groups) {
      if (got[i].hasIds) problems.push(`${here} twinIdList on a non-leaf`);
      compareTree(got[i].groups || [], want[i].groups, problems, here, types.slice(1));
    } else if (got[i].ids.join() !== want[i].ids.join()) {
      problems.push(`${here} ids ${got[i].ids.length} != ${want[i].ids.length}`);
    }
    if (problems.length > 8) return;
  }
}
function leafIds(groups) {
  const ids = [];
  const walk = (nodes) => {
    for (const node of nodes || []) {
      if (node.groups) walk(node.groups);
      else ids.push(...(node.ids || []));
    }
  };
  walk(groups);
  return ids;
}
function expectError(name, response, code, text) {
  const details = `${response.statusDetails || ""} ${response.msg || ""}`;
  add(name, response.__http === 400 && response.status === code && (!text || details.toLowerCase().includes(text.toLowerCase())), {
    http: response.__http, status: response.status, msg: response.msg, statusDetails: response.statusDetails,
  });
}

const twins = psqlJson(`
SELECT coalesce(json_agg(row_to_json(x)), '[]'::json)::text FROM (
  SELECT t.id::text AS id, t.twin_status_id::text AS status, t.assigner_user_id::text AS assignee,
         row_number() OVER (ORDER BY t.created_at DESC, t.id ASC) AS created_rank,
         row_number() OVER (ORDER BY t.name ASC, t.id ASC) AS name_rank
  FROM twin t WHERE t.twin_class_id = '${TASK}'
) x`);
const users = new Map(Object.entries(psqlJson(`
SELECT coalesce(json_object_agg(id::text, name), '{}'::json)::text FROM "user"
WHERE id IN (SELECT assigner_user_id FROM twin WHERE twin_class_id = '${TASK}' AND assigner_user_id IS NOT NULL)
`) || {}));
function ends(linkId, forward) {
  const column = forward ? "dst_twin_id" : "src_twin_id";
  const leaf = forward ? "src_twin_id" : "dst_twin_id";
  const rows = psqlJson(`
    SELECT coalesce(json_agg(json_build_object('leaf', ${leaf}, 'node', ${column})), '[]'::json)::text
    FROM twin_link WHERE link_id = '${linkId}'
  `) || [];
  const map = new Map();
  for (const row of rows) {
    if (!map.has(row.leaf)) map.set(row.leaf, []);
    map.get(row.leaf).push(row.node);
  }
  return map;
}
const milestoneForward = psql(`SELECT (src_twin_class_id = '${TASK}')::text FROM link WHERE id = '${LINK_M}'`) === "true";
const safetyForward = psql(`SELECT (src_twin_class_id = '${TASK}')::text FROM link WHERE id = '${LINK_S}'`) === "true";
const milestoneEnds = ends(LINK_M, milestoneForward);
const safetyEnds = ends(LINK_S, safetyForward);
const nodeNames = new Map(Object.entries(psqlJson(`
SELECT coalesce(json_object_agg(id::text, to_jsonb(name)), '{}'::json)::text FROM twin
WHERE id IN (
  SELECT dst_twin_id FROM twin_link WHERE link_id IN ('${LINK_M}','${LINK_S}')
  UNION SELECT src_twin_id FROM twin_link WHERE link_id IN ('${LINK_M}','${LINK_S}')
)
`) || {}).map(([id, name]) => [id, name]));
const workByTwin = new Map((psqlJson(`
SELECT coalesce(json_agg(json_build_object('id', twin_id, 'option', data_list_option_id)), '[]'::json)::text
FROM twin_field_data_list WHERE twin_class_field_id = '${WORK}'
`) || []).map((row) => [row.id, row.option]));
twins.sort((a, b) => a.created_rank - b.created_rank);
const byNameOrder = [...twins].sort((a, b) => a.name_rank - b.name_rank);
console.log(`tasks=${twins.length} milestoneForward=${milestoneForward} linked=${milestoneEnds.size} safetyForward=${safetyForward} safetyLinked=${safetyEnds.size}`);

const nameSort = byName(users, true);
const linkName = byName(nodeNames, true);

try {
  const response = await api(body([
    field(STATUS, "KEY_ASC"),
    field(ASSIGNEE, "NAME_ASC"),
    field(MILESTONE, "NAME_ASC"),
  ], {}, [{ twinClassFieldId: CREATED, direction: "DESC" }]));
  const want = build(twins, (twin) => [
    [twin.status],
    [twin.assignee ?? null],
    milestoneEnds.get(twin.id)?.length ? [...new Set(milestoneEnds.get(twin.id))] : [null],
  ], [byKey(true), nameSort, linkName]);
  const got = actual(response.groups);
  const problems = [];
  if (response.__http !== 200 || response.status !== 0) problems.push({ http: response.__http, status: response.status, statusDetails: response.statusDetails });
  else compareTree(got, want, problems, "root", ["STATUS", "USER", "TWIN"]);
  const sum = got.reduce((total, group) => total + group.count, 0);
  const ids = new Set(leafIds(got));
  add("status, assignee and milestone link field match the flat selection", problems.length === 0 && sum === twins.length && ids.size === twins.length && got.every((group) => !group.hasTwins), problems.length ? problems : { roots: got.length, sum, leaves: ids.size, ms: response.__ms });

  let nullUserLast = true;
  let nullTwin = false;
  const walk = (nodes, depth) => {
    nodes.forEach((node, index) => {
      if (depth === 1 && node.key == null && index !== nodes.length - 1) nullUserLast = false;
      if (depth === 2 && node.key == null && node.type === "TWIN") nullTwin = true;
      if (node.groups) walk(node.groups, depth + 1);
    });
  };
  walk(got, 0);
  add("empty assignee is last and a task without the milestone link keeps type TWIN", nullUserLast && nullTwin, { nullUserLast, nullTwin });

  const map = response.relatedObjects?.twinMap || {};
  const userMap = response.relatedObjects?.userMap || {};
  const statusMap = response.relatedObjects?.statusMap || {};
  const missingUsers = got.flatMap((status) => status.groups || []).filter((node) => node.key && !userMap[node.key]).length;
  const missingStatuses = got.filter((node) => node.key && !statusMap[node.key]).length;
  const missingLeaves = [...ids].filter((id) => !map[id]).length;
  add("users, statuses and every leaf are in related objects; a leaf is stored once", missingUsers === 0 && missingStatuses === 0 && missingLeaves === 0, {
    missingUsers, missingStatuses, missingLeaves, twinMap: Object.keys(map).length, leaves: ids.size,
  });

  const hidden = await api(body([field(ASSIGNEE, "KEY_ASC")]), "lazyRelation=false");
  const hiddenIds = leafIds(actual(hidden.groups));
  add("without showTwinMode the leaf ids stay and twinMap is absent", hidden.__http === 200 && hiddenIds.length === twins.length && !hidden.relatedObjects?.twinMap, {
    ids: hiddenIds.length, twinMap: hidden.relatedObjects?.twinMap ? Object.keys(hidden.relatedObjects.twinMap).length : 0,
  });
} catch (error) {
  add("tree scenarios threw", false, String(error.stack || error).slice(0, 500));
}

try {
  const response = await api(body([field(SAFETY, "KEY_ASC")], {}, [{ twinClassFieldId: CREATED, direction: "DESC" }]));
  const want = build(twins, (twin) => [safetyEnds.get(twin.id)?.length ? [...new Set(safetyEnds.get(twin.id))] : [null]], [byKey(true)]);
  const got = actual(response.groups);
  const problems = [];
  if (response.__http !== 200) problems.push(response.statusDetails);
  else compareTree(got, want, problems, "safety", ["TWIN"]);
  const sum = got.reduce((total, group) => total + group.count, 0);
  const unique = new Set(leafIds(got));
  const multi = [...safetyEnds.entries()].find(([, nodes]) => new Set(nodes).size > 1);
  add("a link field with several ends counts the task once per node and once overall", problems.length === 0 && unique.size === twins.length && sum > unique.size && Boolean(multi), problems.length ? problems : {
    groups: got.length, sum, unique: unique.size, multi: multi?.[0],
  });
} catch (error) {
  add("link multiplicity threw", false, String(error.stack || error).slice(0, 400));
}

try {
  const response = await api(body([field(WORK, "KEY_ASC")]));
  const want = build(twins, (twin) => [[workByTwin.has(twin.id) ? workByTwin.get(twin.id) : null]], [byKey(true)]);
  const got = actual(response.groups);
  const problems = [];
  if (response.__http !== 200) problems.push(response.statusDetails);
  else compareTree(got, want, problems, "work", ["DATA_LIST_OPTION"]);
  const options = response.relatedObjects?.dataListsOptionMap || {};
  const missing = got.filter((group) => group.key && !options[group.key]).map((group) => group.key);
  add("workType is a DATA_LIST_OPTION and the option is in related objects", problems.length === 0 && missing.length === 0, problems.length ? problems : { groups: got.length, options: Object.keys(options).length });

  const counted = await api(body([field(ASSIGNEE, "COUNT_DESC")]));
  const countedGot = actual(counted.groups);
  const nullIndex = countedGot.findIndex((group) => group.key == null);
  add("COUNT_DESC keeps the empty assignee last", nullIndex === countedGot.length - 1, {
    nullIndex, last: countedGot.length - 1, nullCount: countedGot[nullIndex]?.count, first: countedGot[0]?.count,
  });
} catch (error) {
  add("workType or count sort threw", false, String(error.stack || error).slice(0, 400));
}

try {
  expectError("four levels are rejected before the search", await api(body([field(STATUS), field(ASSIGNEE), field(MILESTONE), field(WORK)])), 13701, "levels");
  expectError("two classes are rejected", await api({
    search: { twinClassIdList: [TASK, "458c6d7d-99c8-4d87-89c6-2f72d0f5d673"] },
    levels: [field(ASSIGNEE)],
  }), 11604, "exactly one");
  expectError("the same field twice is rejected", await api(body([field(STATUS), field(STATUS)])), 11604, "duplicate");
  expectError("NAME_ASC on status is rejected", await api(body([field(STATUS, "NAME_ASC")])), 11604, "NAME sort");
  expectError("NAME_ASC on workType is rejected", await api(body([field(WORK, "NAME_ASC")])), 11604, "NAME sort");
  expectError("a user field is not countable", await api(body([field(USER_FIELD, "KEY_ASC")])), 11604, "not countable");
  const foreign = await api({
    search: { twinClassIdList: [TASK], twinIdList: ["cf850c7e-d8cd-498d-bb97-47101fa86517"] },
    levels: [field(ASSIGNEE, "KEY_ASC")],
  });
  add("a twin from another domain yields an empty 200", foreign.__http === 200 && foreign.status === 0 && (foreign.groups || []).length === 0, {
    http: foreign.__http, groups: foreign.groups?.length,
  });
  const narrowed = await api(body([field(ASSIGNEE, "NAME_ASC")], { statusIdList: [STATUS_MID] }));
  const narrowedGot = actual(narrowed.groups);
  add("a status filter is applied before grouping", narrowed.__http === 200 && narrowedGot.reduce((sum, group) => sum + group.count, 0) === 64, {
    groups: narrowedGot.length, sum: narrowedGot.reduce((sum, group) => sum + group.count, 0),
  });
} catch (error) {
  add("validation threw", false, String(error.stack || error).slice(0, 400));
}

try {
  const decimal = await api(body([field(DECIMAL, "KEY_ASC")]));
  const text = JSON.stringify(decimal.groups || []);
  const zeros = ["0.00", "0.0", "\"value\":0", "\"value\":0.0", "\"value\":0.00"].filter((token) => text.includes(token));
  const zeroGroups = (decimal.groups || []).filter((group) => group.key?.type === "DECIMAL" && Number(group.key.value) === 0);
  add("decimal zero is one group", decimal.__http === 200 && zeroGroups.length === 1, { http: decimal.__http, zeroGroups: zeroGroups.length, counts: zeroGroups.map((group) => group.count) });

  const stamp = await api({
    search: { twinClassIdList: ["b00b43a8-c1ab-3555-9c99-3442e004432f"] },
    levels: [field("ae6118ef-f588-3b8b-a87a-5d061e12378e", "KEY_ASC")],
  });
  add("timestamp key keeps the stored clock time", stamp.groups?.[0]?.key?.value === "2026-01-01T00:00:00" || stamp.groups?.[0]?.key?.value === "2026-01-01 00:00:00", {
    type: stamp.groups?.[0]?.key?.type, value: stamp.groups?.[0]?.key?.value,
  });
  const bool = await api(body([field(BOOL, "KEY_ASC")]));
  const boolKeys = (bool.groups || []).map((group) => group.key?.value);
  add("boolean groups are false, true and null", bool.__http === 200 && boolKeys.includes(false) && boolKeys.includes(true) && boolKeys.includes(null) && bool.groups.at(-1)?.key?.value == null, { keys: boolKeys });
} catch (error) {
  add("scalar fields threw", false, String(error.stack || error).slice(0, 400));
}

const summary = {
  ranAt: new Date().toISOString(),
  passed: checks.filter((item) => item.ok).length,
  failed: checks.filter((item) => !item.ok).length,
  checks,
};
writeFileSync(join(OUT, "checks.json"), JSON.stringify(summary, null, 2));
console.log(`DONE passed=${summary.passed} failed=${summary.failed}`);
if (summary.failed) process.exitCode = 1;
