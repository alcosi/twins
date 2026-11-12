import { execFileSync } from "node:child_process";
import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const OUT = dirname(fileURLToPath(import.meta.url));
mkdirSync(join(OUT, "raw"), { recursive: true });

const BASE = "http://127.0.0.1:8080";
const HEADERS = {
  DomainId: "f67ad556-dd27-4871-9a00-16fb0e8a4102",
  AuthToken: "608c6d7d-99c8-4d87-89c6-2f72d0f5d673,9a3f6075-f175-41cd-a804-934201ec969c",
  Channel: "WEB",
  "Content-Type": "application/json",
  Accept: "application/json",
};
const DOMAIN = "f67ad556-dd27-4871-9a00-16fb0e8a4102";
const TASK = "7c027b60-0f6c-445c-9889-8ee3855d2c59";
const ASSIGNEE = "00000000-0000-0000-0011-000000000007";
const OWNER = "00000000-0000-0000-0011-000000000006";
const CREATOR = "00000000-0000-0000-0011-000000000008";
const HEAD = "00000000-0000-0000-0011-000000000009";
const STATUS = "00000000-0000-0000-0011-000000000010";
const CREATED = "00000000-0000-0000-0011-000000000011";
const NAME = "00000000-0000-0000-0011-000000000003";
const CLASS = "00000000-0000-0000-0011-000000000013";
const FLAVOR = "00000000-0000-0000-0011-000000000017";
const WORK_TYPE = "95f2c8ca-8b79-4971-9726-270a11c7d031";
const BOOL_FIELD = "19e3f226-5249-46eb-8d01-e9a054494715";
const DECIMAL_FIELD = "e454db8e-c8d8-48cb-9684-00abef38ee9e";
const USER_FIELD = "27af06b8-f8c9-4603-bc40-f4f6259630c0";
const LINK_FIELD = "6ecf4b17-791b-43d7-aaab-cfa9185fe6df";
const SPACE_FIELD = "93878d84-20a3-4479-abed-5712cdbfcd6b";
const CALC_FIELD = "e35763f8-34a6-40d9-aa26-5aca6294bc2e";
const ATTACH_FIELD = "2121d75d-8fee-45f1-bf51-cb787b834596";
const STATUS_MID = "c432b42e-0dfe-4c9d-8c21-5a54aa7b1257";
const STATUS_SMALL = "8e6f863b-9a69-48aa-8deb-e98ec271e051";
const SHOW = "showTwinMode=DETAILED&showTwin2UserMode=SHORT&showTwin2StatusMode=SHORT&showTwin2TwinClassMode=SHORT&showDataListOptionMode=SHORT&lazyRelation=false";
const PAGE = 10;

const checks = [];

function add(name, ok, detail) {
  const item = { name, ok: Boolean(ok), detail };
  checks.push(item);
  console.log((ok ? "PASS " : "FAIL ") + name + (detail ? " — " + clip(detail) : ""));
  return item;
}

function clip(value) {
  const text = typeof value === "string" ? value : JSON.stringify(value);
  return text.length > 500 ? text.slice(0, 500) + "…" : text;
}

function psql(sql) {
  return execFileSync(
    "docker",
    ["exec", "-i", "7bd7c8f7c221", "psql", "-U", "postgres", "-d", "wnr", "-v", "ON_ERROR_STOP=1", "-q", "-t", "-A", "-c", sql],
    { encoding: "utf8", maxBuffer: 64 * 1024 * 1024 }
  ).trim();
}

function psqlJson(sql) {
  const text = psql(sql);
  return text ? JSON.parse(text) : null;
}

async function api(path, body, query = SHOW, timeoutMs = 180000) {
  const started = Date.now();
  const response = await fetch(BASE + path + (query ? "?" + query : ""), {
    method: "POST",
    headers: HEADERS,
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(timeoutMs),
  });
  const text = await response.text();
  let parsed;
  try {
    parsed = text ? JSON.parse(text) : {};
  } catch {
    parsed = { raw: text.slice(0, 1500) };
  }
  parsed.__ms = Date.now() - started;
  parsed.__http = response.status;
  console.log(`  ${response.status} ${parsed.__ms}ms ${path} groups=${parsed.groups?.length ?? "-"} status=${parsed.status}`);
  return parsed;
}

function taskSearch(extra = {}) {
  return { twinClassIdList: [TASK], ...extra };
}

function req(groupFieldId, groupSort, memberSorts, search = taskSearch()) {
  const body = { search, groupFieldId };
  if (groupSort !== undefined) body.groupSort = groupSort;
  if (memberSorts !== undefined) body.memberSorts = memberSorts;
  return body;
}

function sort(field, direction) {
  return [{ twinClassFieldId: field, direction }];
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

const SIGN = 1n << 63n;

function uuidHalves(value) {
  const hex = String(value).replace(/-/g, "");
  let hi = BigInt("0x" + hex.slice(0, 16));
  let lo = BigInt("0x" + hex.slice(16));
  if (hi >= SIGN) hi -= SIGN << 1n;
  if (lo >= SIGN) lo -= SIGN << 1n;
  return [hi, lo];
}

function uuidCompare(left, right) {
  const [lhi, llo] = uuidHalves(left);
  const [rhi, rlo] = uuidHalves(right);
  if (lhi !== rhi) return lhi < rhi ? -1 : 1;
  if (llo !== rlo) return llo < rlo ? -1 : 1;
  return 0;
}

function isUuid(value) {
  return typeof value === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value);
}

function compareRaw(left, right, ascending) {
  if (left == null || right == null) return nullsLast(left, right);
  let compared;
  if (isUuid(left) && isUuid(right)) compared = uuidCompare(left, right);
  else if (typeof left === "number" && typeof right === "number") compared = left < right ? -1 : left > right ? 1 : 0;
  else if (typeof left === "boolean" && typeof right === "boolean") compared = left === right ? 0 : left ? 1 : -1;
  else compared = String(left) < String(right) ? -1 : String(left) > String(right) ? 1 : 0;
  return ascending ? compared : -compared;
}

function byName(names, ascending) {
  return (left, right) => {
    if (left.key == null || right.key == null) return nullsLast(left.key, right.key);
    const compared = compareText(names.get(left.key) ?? null, names.get(right.key) ?? null, ascending);
    if (compared !== 0) return compared;
    return uuidCompare(left.key, right.key);
  };
}

function byCount(ascending) {
  return (left, right) => {
    let compared = left.twins.length - right.twins.length;
    if (!ascending) compared = -compared;
    if (compared !== 0) return compared;
    return compareRaw(left.key, right.key, true);
  };
}

function byKey(ascending) {
  return (left, right) => compareRaw(left.key, right.key, ascending);
}

function byCreated(left, right) {
  if (left.createdAt !== right.createdAt) return left.createdAt < right.createdAt ? 1 : -1;
  return uuidCompare(left.id, right.id);
}

function byLeafName(left, right) {
  const compared = compareText(left.name ?? null, right.name ?? null, true);
  if (compared !== 0) return compared;
  return uuidCompare(left.id, right.id);
}

function expectedGroups(twins, keysOf, leafCompare, groupCompare) {
  const by = new Map();
  for (const twin of twins) {
    let keys = keysOf(twin);
    if (!keys || keys.length === 0) keys = [null];
    const seen = new Set();
    for (const key of keys) {
      const token = key == null ? "\0" : JSON.stringify(key);
      if (seen.has(token)) continue;
      seen.add(token);
      let group = by.get(token);
      if (!group) {
        group = { key: key ?? null, twins: [] };
        by.set(token, group);
      }
      group.twins.push(twin);
    }
  }
  const groups = [...by.values()];
  for (const group of groups) group.twins.sort(leafCompare);
  groups.sort(groupCompare);
  return groups;
}

function idsOf(group) {
  return (group.twins || []).map((twin) => twin.id);
}

function sameOrder(actual, expected) {
  if (actual.length !== expected.length) return { ok: false, at: 0, actual: actual.length, expected: expected.length };
  for (let i = 0; i < actual.length; i++) {
    if (actual[i] !== expected[i]) return { ok: false, at: i, actual: actual[i], expected: expected[i] };
  }
  return { ok: true };
}

function normKey(value) {
  if (value == null) return null;
  if (typeof value === "string" && isUuid(value)) return value.toLowerCase();
  return value;
}

function apiGroups(body, prop) {
  return (body.groups || []).map((group) => {
    if (prop === "groupedFieldValues") {
      const map = group.groupedFieldValues || {};
      const fieldId = Object.keys(map)[0] || null;
      const present = fieldId != null && Object.prototype.hasOwnProperty.call(map, fieldId);
      return {
        key: present ? normKey(map[fieldId]) : undefined,
        present,
        fieldId,
        count: group.count,
        twins: group.twins || [],
        rawKeys: Object.keys(group),
      };
    }
    const present = Object.prototype.hasOwnProperty.call(group, prop);
    return {
      key: present ? normKey(group[prop]) : undefined,
      present,
      count: group.count,
      twins: group.twins || [],
      rawKeys: Object.keys(group),
    };
  });
}

function indexById(rows) {
  const map = new Map();
  for (const row of rows) map.set(row.id, row);
  return map;
}

function columnKeys(row, column) {
  return [row[column] ?? null];
}

function compareBasic(name, body, twins, column, prop, leafCompare, groupCompare, options = {}) {
  const problems = [];
  if (body.__http !== 200 || body.status !== 0) {
    return add(name, false, { http: body.__http, status: body.status, msg: body.msg, statusDetails: body.statusDetails });
  }
  if ("pagination" in body) problems.push("pagination present");
  const actual = apiGroups(body, prop);
  const expected = expectedGroups(twins, (row) => columnKeys(row, column), leafCompare, groupCompare);
  if (actual.length !== expected.length) problems.push(`groups ${actual.length} != ${expected.length}`);
  const limit = Math.max(actual.length, expected.length);
  let leafMismatches = 0;
  let keyMismatches = 0;
  const seen = new Set();
  for (let i = 0; i < limit; i++) {
    const got = actual[i];
    const want = expected[i];
    if (!got || !want || JSON.stringify(got.key) !== JSON.stringify(want.key)) {
      keyMismatches++;
      if (keyMismatches <= 3) problems.push(`key[${i}] ${got ? JSON.stringify(got.key) : "missing"} != ${want ? JSON.stringify(want.key) : "missing"}`);
      continue;
    }
    if (!got.present) problems.push(`key[${i}] property ${prop} missing`);
    if (Number(got.count) !== got.twins.length) problems.push(`count ${got.count} != twins ${got.twins.length} at ${got.key}`);
    if (got.twins.length !== want.twins.length) problems.push(`size ${got.twins.length} != ${want.twins.length} at ${got.key}`);
    const order = sameOrder(idsOf(got), idsOf(want));
    if (!order.ok) {
      leafMismatches++;
      if (leafMismatches <= 2) problems.push(`order at ${JSON.stringify(got.key)} index ${order.at}: ${order.actual} != ${order.expected}`);
    }
    for (const twin of got.twins) {
      seen.add(twin.id);
      if (options.leafAssert) {
        const issue = options.leafAssert(twin, got.key);
        if (issue) problems.push(issue);
      }
    }
    if (options.related) {
      const issue = options.related(body, got.key);
      if (issue) problems.push(issue);
    }
  }
  const expectedIds = new Set(twins.map((twin) => twin.id));
  if (options.uniqueCoverage !== false) {
    if (seen.size !== expectedIds.size) problems.push(`unique leaves ${seen.size} != twins ${expectedIds.size}`);
    for (const id of expectedIds) if (!seen.has(id)) {
      problems.push(`missing twin ${id}`);
      break;
    }
  }
  const nullGroup = actual.find((group) => group.key == null);
  const nullLast = !nullGroup || actual[actual.length - 1]?.key == null;
  return add(name, problems.length === 0, problems.length ? problems : {
    groups: actual.length,
    twins: seen.size,
    nullGroup: nullGroup ? nullGroup.twins.length : 0,
    nullLast,
    ms: body.__ms,
    maxGroup: actual.reduce((max, group) => Math.max(max, group.twins.length), 0),
  });
}

function expectError(name, body, code, text) {
  const details = `${body.statusDetails || ""} ${body.msg || ""} ${body.raw || ""}`;
  const ok = body.__http === 400 && body.status === code && (!text || details.toLowerCase().includes(text.toLowerCase()));
  return add(name, ok, { http: body.__http, status: body.status, msg: body.msg, statusDetails: body.statusDetails });
}

function relatedHas(body, mapName, id) {
  const map = body.relatedObjects?.[mapName];
  if (!map || !map[id]) return `relatedObjects.${mapName} missing ${id}`;
  return null;
}

const twins = psqlJson(`
SELECT coalesce(json_agg(json_build_object(
  'id', t.id,
  'name', t.name,
  'createdAt', to_char(t.created_at, 'YYYY-MM-DD"T"HH24:MI:SS.US'),
  'assignerUserId', t.assigner_user_id,
  'ownerUserId', t.owner_user_id,
  'createdByUserId', t.created_by_user_id,
  'headTwinId', t.head_twin_id,
  'twinStatusId', t.twin_status_id,
  'twinClassId', t.twin_class_id,
  'flavorId', t.flavor_data_list_option_id
)), '[]'::json)::text
FROM twin t
WHERE t.twin_class_id = '${TASK}'
`);
const users = psqlJson(`
SELECT coalesce(json_object_agg(u.id::text, u.name), '{}'::json)::text
FROM "user" u
WHERE u.id IN (
  SELECT assigner_user_id FROM twin WHERE twin_class_id = '${TASK}' AND assigner_user_id IS NOT NULL
  UNION SELECT owner_user_id FROM twin WHERE twin_class_id = '${TASK}' AND owner_user_id IS NOT NULL
  UNION SELECT created_by_user_id FROM twin WHERE twin_class_id = '${TASK}' AND created_by_user_id IS NOT NULL
)
`);
const heads = psqlJson(`
SELECT coalesce(json_object_agg(h.id::text, to_jsonb(h.name)), '{}'::json)::text
FROM twin h
WHERE h.id IN (SELECT head_twin_id FROM twin WHERE twin_class_id = '${TASK}' AND head_twin_id IS NOT NULL)
`);
const workRows = psqlJson(`
SELECT coalesce(json_agg(json_build_object('twinId', twin_id, 'optionId', data_list_option_id)), '[]'::json)::text
FROM twin_field_data_list WHERE twin_class_field_id = '${WORK_TYPE}'
`);
const boolRows = psqlJson(`
SELECT coalesce(json_agg(json_build_object('twinId', twin_id, 'value', value)), '[]'::json)::text
FROM twin_field_boolean WHERE twin_class_field_id = '${BOOL_FIELD}'
`);
const decimalRows = psqlJson(`
SELECT coalesce(json_agg(json_build_object('twinId', twin_id, 'value', value)), '[]'::json)::text
FROM twin_field_decimal WHERE twin_class_field_id = '${DECIMAL_FIELD}'
`);
const foreignId = psql(`
SELECT t.id::text FROM twin t
JOIN twin_class c ON c.id = t.twin_class_id
WHERE c.domain_id IS DISTINCT FROM '${DOMAIN}'
LIMIT 1
`);
const multipleFields = Number(psql(`
SELECT count(*) FROM twin_class_field f
JOIN twin_class c ON c.id = f.twin_class_id
WHERE c.domain_id = '${DOMAIN}' AND f.field_typer_params -> 'multiple' = 'true'
`));

const userNames = new Map(Object.entries(users || {}));
const headNames = new Map(Object.entries(heads || {}).map(([id, name]) => [id, name]));
const byId = indexById(twins);
console.log(`DB tasks=${twins.length} users=${userNames.size} heads=${headNames.size} workRows=${workRows.length} boolRows=${boolRows.length} decimalRows=${decimalRows.length} multipleFields=${multipleFields} foreign=${foreignId}`);

function attachValues(rows, valueName) {
  const values = new Map();
  for (const row of rows) {
    if (!values.has(row.twinId)) values.set(row.twinId, []);
    values.get(row.twinId).push(row[valueName] ?? null);
  }
  return twins.map((twin) => ({ ...twin, values: values.get(twin.id) || [null] }));
}

const workTwins = attachValues(workRows, "optionId");
const boolTwins = attachValues(boolRows, "value");
const decimalTwins = attachValues(decimalRows, "value");

function leafAssignee(twin, key) {
  const got = twin.assignerUserId ?? null;
  if (got !== key) return `leaf ${twin.id} assigner ${got} != ${key}`;
  if (twin.twinClassId !== TASK) return `leaf ${twin.id} class ${twin.twinClassId}`;
  if (!twin.createdAt) return `leaf ${twin.id} has no createdAt`;
  return null;
}

function relatedUser(body, key) {
  if (key == null) return null;
  const issue = relatedHas(body, "userMap", key);
  if (issue) return issue;
  const fullName = body.relatedObjects.userMap[key].fullName ?? null;
  const dbName = userNames.get(key) ?? null;
  if (fullName !== dbName) return `user ${key} fullName ${JSON.stringify(fullName)} != db ${JSON.stringify(dbName)}`;
  return null;
}

const nameAsc = byName(userNames, true);
const created = byCreated;

let assigneeBody;
let assigneeAgain;

try {
  assigneeBody = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "NAME_ASC", sort(CREATED, "DESC")));
  compareBasic("S1 assignee NAME_ASC, leaves createdAt DESC", assigneeBody, twins, "assignerUserId", "assignerUserId", created, nameAsc, {
    leafAssert: leafAssignee,
    related: relatedUser,
  });
  const actual = apiGroups(assigneeBody, "assignerUserId");
  const maxGroup = actual.reduce((max, group) => Math.max(max, group.twins.length), 0);
  add("S4 biggest assignee group is larger than a search page and complete", maxGroup > PAGE && actual.every((group) => Number(group.count) === group.twins.length), { maxGroup, page: PAGE, groups: actual.length });
  add("S5 one response holds every assignee group", actual.length > PAGE, { groups: actual.length, page: PAGE });
  const nullGroup = actual.find((group) => group.key == null);
  const nullRaw = (assigneeBody.groups || []).find((group) => group.assignerUserId == null && Object.prototype.hasOwnProperty.call(group, "assignerUserId"));
  add("S2 null assignee group is present, last, and serialized as null", Boolean(nullGroup) && actual[actual.length - 1].key == null && Boolean(nullRaw) && nullRaw.assignerUserId === null, {
    count: nullGroup?.twins.length,
    last: actual[actual.length - 1]?.key ?? null,
    serialized: nullRaw ? nullRaw.assignerUserId : "property missing",
  });
  const sample = nullRaw ? { assignerUserId: nullRaw.assignerUserId, count: nullRaw.count, twinCount: (nullRaw.twins || []).length, keys: Object.keys(nullRaw) } : null;
  writeFileSync(join(OUT, "raw", "s2-null-assignee-group.json"), JSON.stringify(sample, null, 2));
  const onlyKey = actual.every((group) => {
    const foreign = group.rawKeys.filter((key) => ["ownerUserId", "createdByUserId", "twinStatusId", "twinClassId", "headTwinId", "flavorDataListOptionId", "groupedFieldValues", "key"].includes(key));
    return foreign.length === 0 && group.rawKeys.includes("assignerUserId");
  });
  add("S1 group JSON has only assignerUserId as the key", onlyKey, actual[0]?.rawKeys);

  assigneeAgain = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "NAME_ASC", sort(CREATED, "DESC")));
  const again = apiGroups(assigneeAgain, "assignerUserId");
  const stable = sameOrder(again.flatMap(idsOf), actual.flatMap(idsOf)) && sameOrder(again.map((group) => JSON.stringify(group.key)), actual.map((group) => JSON.stringify(group.key)));
  add("S5 repeated request returns the same groups and leaf order", stable.ok, stable);

  const desc = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "NAME_DESC", sort(CREATED, "DESC")));
  compareBasic("assignee NAME_DESC keeps the empty key last", desc, twins, "assignerUserId", "assignerUserId", created, byName(userNames, false), {
    leafAssert: leafAssignee,
  });

  const keyAsc = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "KEY_ASC", sort(CREATED, "DESC")));
  compareBasic("assignee KEY_ASC, empty key last", keyAsc, twins, "assignerUserId", "assignerUserId", created, byKey(true), { leafAssert: leafAssignee });

  const keyDesc = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "KEY_DESC", sort(CREATED, "DESC")));
  compareBasic("assignee KEY_DESC, empty key still last", keyDesc, twins, "assignerUserId", "assignerUserId", created, byKey(false), { leafAssert: leafAssignee });

  const counted = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "COUNT_DESC", sort(NAME, "ASC")));
  compareBasic("S10 COUNT_DESC groups and name ASC leaves", counted, twins, "assignerUserId", "assignerUserId", byLeafName, byCount(false), { leafAssert: leafAssignee });
  const countedGroups = apiGroups(counted, "assignerUserId");
  const nullCounted = countedGroups.findIndex((group) => group.key == null);
  add("COUNT_DESC places the empty key by size, not forced last", nullCounted >= 0, {
    nullIndex: nullCounted,
    lastIndex: countedGroups.length - 1,
    nullCount: countedGroups[nullCounted]?.twins.length,
    firstCount: countedGroups[0]?.twins.length,
  });
} catch (error) {
  add("assignee scenarios threw", false, String(error));
}

try {
  const filteredTwins = twins.filter((twin) => twin.twinStatusId === STATUS_MID);
  const filtered = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "NAME_ASC", sort(CREATED, "DESC"), taskSearch({ statusIdList: [STATUS_MID] })));
  compareBasic("S3 status filter narrows assignee groups", filtered, filteredTwins, "assignerUserId", "assignerUserId", created, nameAsc, { leafAssert: leafAssignee, related: relatedUser });
  const fullKeys = new Set(apiGroups(assigneeBody, "assignerUserId").map((group) => JSON.stringify(group.key)));
  const partKeys = apiGroups(filtered, "assignerUserId").map((group) => JSON.stringify(group.key));
  const dropped = [...fullKeys].filter((key) => !partKeys.includes(key));
  add("S3 at least one assignee from the full set disappears", dropped.length > 0 && partKeys.every((key) => fullKeys.has(key)), { full: fullKeys.size, filtered: partKeys.length, dropped: dropped.length });

  const paged = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "NAME_ASC", sort(CREATED, "DESC"), taskSearch({ statusIdList: [STATUS_MID] })), SHOW + "&offset=5&limit=1");
  const unpagedIds = apiGroups(filtered, "assignerUserId").flatMap(idsOf);
  const pagedIds = apiGroups(paged, "assignerUserId").flatMap(idsOf);
  add("offset and limit query params do not cut the tree", paged.__http === 200 && sameOrder(pagedIds, unpagedIds).ok && !("pagination" in paged), {
    http: paged.__http,
    paged: pagedIds.length,
    unpaged: unpagedIds.length,
  });
} catch (error) {
  add("S3 threw", false, String(error));
}

try {
  const smallTwins = twins.filter((twin) => twin.twinStatusId === STATUS_SMALL);
  const omitted = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, undefined, undefined, taskSearch({ statusIdList: [STATUS_SMALL] })));
  compareBasic("default groupSort for assignee is NAME_ASC and default leaves are createdAt DESC, id ASC", omitted, smallTwins, "assignerUserId", "assignerUserId", created, nameAsc, { leafAssert: leafAssignee });
  const explicit = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "NAME_ASC", sort(CREATED, "DESC"), taskSearch({ statusIdList: [STATUS_SMALL] })));
  add("omitted memberSorts matches explicit createdAt DESC", sameOrder(apiGroups(omitted, "assignerUserId").flatMap(idsOf), apiGroups(explicit, "assignerUserId").flatMap(idsOf)).ok, { twins: smallTwins.length });
} catch (error) {
  add("defaults threw", false, String(error));
}

try {
  const statusBody = await api("/private/twin/search/grouped/v1", req(STATUS, undefined, sort(CREATED, "DESC")));
  compareBasic("S6 status default KEY_ASC", statusBody, twins, "twinStatusId", "twinStatusId", created, byKey(true), {
    leafAssert: (twin, key) => (twin.statusId !== key ? `leaf ${twin.id} status ${twin.statusId} != ${key}` : null),
    related: (body, key) => (key == null ? "unexpected empty status" : relatedHas(body, "statusMap", key)),
  });
  const statusName = await api("/private/twin/search/grouped/v1", req(STATUS, "NAME_ASC", sort(CREATED, "DESC")));
  expectError("S6 NAME_ASC on status is 400", statusName, 11604, "NAME sort");
  const className = await api("/private/twin/search/grouped/v1", req(CLASS, "NAME_ASC"));
  expectError("NAME_ASC on class is 400", className, 11604, "NAME sort");
  const classBody = await api("/private/twin/search/grouped/v1", req(CLASS));
  compareBasic("class default KEY_ASC is one group", classBody, twins, "twinClassId", "twinClassId", created, byKey(true), {
    leafAssert: (twin, key) => (twin.twinClassId !== key ? `class ${twin.twinClassId}` : null),
    related: (body, key) => relatedHas(body, "twinClassMap", key),
  });
} catch (error) {
  add("status/class threw", false, String(error));
}

try {
  const headBody = await api("/private/twin/search/grouped/v1", req(HEAD, "NAME_ASC", sort(CREATED, "DESC")));
  compareBasic("S7 head NAME_ASC", headBody, twins, "headTwinId", "headTwinId", created, byName(headNames, true), {
    leafAssert: (twin, key) => (twin.headTwinId !== key ? `leaf ${twin.id} head ${twin.headTwinId} != ${key}` : null),
    related: (body, key) => (key == null ? null : relatedHas(body, "twinMap", key)),
  });
} catch (error) {
  add("S7 threw", false, String(error));
}

try {
  const ownerBody = await api("/private/twin/search/grouped/v1", req(OWNER, "NAME_ASC", sort(CREATED, "DESC")));
  compareBasic("owner NAME_ASC, empty key last", ownerBody, twins, "ownerUserId", "ownerUserId", created, nameAsc, {
    leafAssert: (twin, key) => ((twin.ownerUserId ?? null) !== key ? `owner ${twin.ownerUserId} != ${key}` : null),
    related: relatedUser,
  });
  const creatorBody = await api("/private/twin/search/grouped/v1", req(CREATOR, "NAME_ASC", sort(CREATED, "DESC")));
  compareBasic("creator NAME_ASC", creatorBody, twins, "createdByUserId", "createdByUserId", created, nameAsc, {
    leafAssert: (twin, key) => ((twin.authorUserId ?? null) !== key ? `author ${twin.authorUserId} != ${key}` : null),
    related: relatedUser,
  });
} catch (error) {
  add("owner/creator threw", false, String(error));
}

try {
  const flavorName = await api("/private/twin/search/grouped/v1", req(FLAVOR, "NAME_ASC"));
  expectError("NAME_ASC on flavor is 400", flavorName, 11604, "NAME sort");
  const flavorBody = await api("/private/twin/search/grouped/v1", req(FLAVOR));
  compareBasic("flavor default KEY_ASC, empty key last", flavorBody, twins, "flavorId", "flavorDataListOptionId", created, byKey(true), {
    leafAssert: (twin, key) => ((twin.flavorDataListOptionId ?? null) !== key ? `flavor ${twin.flavorDataListOptionId} != ${key}` : null),
    related: (body, key) => (key == null ? null : relatedHas(body, "dataListsOptionMap", key)),
  });
} catch (error) {
  add("flavor threw", false, String(error));
}

try {
  const workBody = await api("/private/twin/search/grouped/v1", req(WORK_TYPE));
  const workActual = apiGroups(workBody, "groupedFieldValues");
  const workExpected = expectedGroups(workTwins, (row) => row.values, created, byKey(true));
  const problems = [];
  if (workBody.__http !== 200 || workBody.status !== 0) problems.push({ http: workBody.__http, status: workBody.status, statusDetails: workBody.statusDetails });
  if (workActual.length !== workExpected.length) problems.push(`groups ${workActual.length} != ${workExpected.length}`);
  let sum = 0;
  const seen = new Set();
  for (let i = 0; i < Math.max(workActual.length, workExpected.length); i++) {
    const got = workActual[i];
    const want = workExpected[i];
    if (!got || !want || JSON.stringify(got.key) !== JSON.stringify(want.key)) {
      problems.push(`key ${JSON.stringify(got?.key)} != ${JSON.stringify(want?.key)}`);
      continue;
    }
    if (got.fieldId !== WORK_TYPE) problems.push(`field ${got.fieldId}`);
    if (Number(got.count) !== got.twins.length || got.twins.length !== want.twins.length) problems.push(`size at ${got.key}`);
    sum += got.twins.length;
    const order = sameOrder(idsOf(got), idsOf(want));
    if (!order.ok) problems.push(`order ${JSON.stringify(got.key)} @${order.at}`);
    for (const twin of got.twins) seen.add(twin.id);
    if (got.key != null) {
      const issue = relatedHas(workBody, "dataListsOptionMap", got.key);
      if (issue) problems.push(issue);
    }
  }
  if (sum !== twins.length) problems.push(`sum ${sum} != ${twins.length}`);
  if (seen.size !== twins.length) problems.push(`unique ${seen.size}`);
  const nullWork = workActual.find((group) => group.key == null);
  add("S11 workType single-select groups, sum equals tasks, options in related objects", problems.length === 0, problems.length ? problems : {
    groups: workActual.length,
    sum,
    nullGroup: nullWork ? nullWork.twins.length : 0,
    ms: workBody.__ms,
  });
  const workName = await api("/private/twin/search/grouped/v1", req(WORK_TYPE, "NAME_ASC"));
  expectError("NAME_ASC on workType data list is 400", workName, 11604, "NAME sort");
} catch (error) {
  add("S11 threw", false, String(error));
}

try {
  const boolBody = await api("/private/twin/search/grouped/v1", req(BOOL_FIELD, "KEY_ASC", sort(CREATED, "DESC")));
  const actual = apiGroups(boolBody, "groupedFieldValues");
  const expected = expectedGroups(boolTwins, (row) => row.values, created, byKey(true));
  const problems = [];
  if (boolBody.__http !== 200) problems.push({ http: boolBody.__http, status: boolBody.status, statusDetails: boolBody.statusDetails });
  if (actual.length !== expected.length) problems.push(`groups ${actual.length} != ${expected.length}`);
  for (let i = 0; i < Math.min(actual.length, expected.length); i++) {
    if (JSON.stringify(actual[i].key) !== JSON.stringify(expected[i].key)) problems.push(`key ${JSON.stringify(actual[i].key)} != ${JSON.stringify(expected[i].key)}`);
    else if (actual[i].twins.length !== expected[i].twins.length) problems.push(`size ${actual[i].key}: ${actual[i].twins.length} != ${expected[i].twins.length}`);
    else {
      const order = sameOrder(idsOf(actual[i]), idsOf(expected[i]));
      if (!order.ok) problems.push(`order ${JSON.stringify(actual[i].key)} @${order.at}`);
    }
  }
  const nullBool = (boolBody.groups || []).find((group) => group.groupedFieldValues && Object.values(group.groupedFieldValues).includes(null));
  const nullValueKept = Boolean(nullBool) && Object.values(nullBool.groupedFieldValues)[0] === null;
  add("boolean field groups true, false and null, empty value kept in the map", problems.length === 0 && nullValueKept, problems.length ? problems : {
    keys: actual.map((group) => group.key),
    sizes: actual.map((group) => group.twins.length),
    nullSerialized: nullValueKept,
  });
  const boolName = await api("/private/twin/search/grouped/v1", req(BOOL_FIELD, "NAME_ASC"));
  expectError("NAME_ASC on boolean is 400", boolName, 11604, "NAME sort");

  const decimalBody = await api("/private/twin/search/grouped/v1", req(DECIMAL_FIELD, "KEY_ASC"));
  const decimalActual = apiGroups(decimalBody, "groupedFieldValues");
  const decimalExpected = expectedGroups(decimalTwins, (row) => row.values.map((value) => (value == null ? null : Number(value))), created, byKey(true));
  const decimalProblems = [];
  if (decimalBody.__http !== 200 || decimalBody.status !== 0) decimalProblems.push({ http: decimalBody.__http, status: decimalBody.status, statusDetails: decimalBody.statusDetails });
  if (decimalActual.length !== decimalExpected.length) decimalProblems.push(`groups ${decimalActual.length} != ${decimalExpected.length}`);
  let decimalSum = 0;
  for (let i = 0; i < Math.min(decimalActual.length, decimalExpected.length); i++) {
    if (JSON.stringify(decimalActual[i].key) !== JSON.stringify(decimalExpected[i].key)) {
      decimalProblems.push(`key ${JSON.stringify(decimalActual[i].key)} != ${JSON.stringify(decimalExpected[i].key)}`);
      continue;
    }
    decimalSum += decimalActual[i].twins.length;
    if (decimalActual[i].twins.length !== decimalExpected[i].twins.length) decimalProblems.push(`size ${decimalActual[i].key}`);
  }
  if (decimalBody.__http === 200 && decimalSum !== twins.length) decimalProblems.push(`sum ${decimalSum} != ${twins.length}`);
  add("decimal progress KEY_ASC groups match stored values", decimalProblems.length === 0, decimalProblems.length ? decimalProblems.slice(0, 8) : { groups: decimalActual.length, sum: decimalSum });
  const decimalName = await api("/private/twin/search/grouped/v1", req(DECIMAL_FIELD, "NAME_ASC"));
  expectError("NAME_ASC on decimal is 400", decimalName, 11604, "NAME sort");
} catch (error) {
  add("boolean/decimal threw", false, String(error));
}

try {
  const hidden = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "KEY_ASC", undefined, { twinIdList: [foreignId] }));
  const hiddenIds = apiGroups(hidden, "assignerUserId").flatMap(idsOf);
  add("S8 twin from another domain is absent and the response is 200", hidden.__http === 200 && hidden.status === 0 && !hiddenIds.includes(foreignId), {
    http: hidden.__http,
    status: hidden.status,
    groups: hidden.groups?.length,
    ids: hiddenIds.length,
  });
  const oneId = twins[0].id;
  const mixed = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "KEY_ASC", undefined, { twinIdList: [oneId, foreignId] }));
  const mixedIds = apiGroups(mixed, "assignerUserId").flatMap(idsOf);
  add("S8 a visible twin stays when an invisible id is added to the filter", mixed.__http === 200 && mixedIds.length === 1 && mixedIds[0] === oneId, { ids: mixedIds });
} catch (error) {
  add("S8 threw", false, String(error));
}

try {
  expectError("S9 name is not groupable", await api("/private/twin/search/grouped/v1", req(NAME, "KEY_ASC")), 11604, "not groupable");
  expectError("S9 createdAt is not groupable", await api("/private/twin/search/grouped/v1", req(CREATED, "KEY_ASC")), 11604, "not groupable");
  expectError("S9 dynamic user field is not countable", await api("/private/twin/search/grouped/v1", req(USER_FIELD, "KEY_ASC")), 11604, "not countable");
  expectError("S9 link field is not countable", await api("/private/twin/search/grouped/v1", req(LINK_FIELD, "KEY_ASC")), 11604, "not countable");
  expectError("S9 space-role user field is not countable", await api("/private/twin/search/grouped/v1", req(SPACE_FIELD, "KEY_ASC")), 11604, "not countable");
  expectError("S9 calc field is not countable", await api("/private/twin/search/grouped/v1", req(CALC_FIELD, "KEY_ASC")), 11604, "not countable");
  expectError("S9 attachment field is not countable", await api("/private/twin/search/grouped/v1", req(ATTACH_FIELD, "KEY_ASC")), 11604, "not countable");
  const unknown = await api("/private/twin/search/grouped/v1", req("11111111-1111-1111-1111-111111111111", "KEY_ASC"));
  add("unknown field id is rejected before a twin page", unknown.__http === 400, { http: unknown.__http, status: unknown.status, statusDetails: unknown.statusDetails, msg: unknown.msg });
  expectError("memberSorts rejects a dynamic field", await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "KEY_ASC", sort(WORK_TYPE, "ASC"), taskSearch({ statusIdList: [STATUS_SMALL] }))), 11604, "not a basic field");
  const threeSorts = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "KEY_ASC", [
    { twinClassFieldId: CREATED, direction: "DESC" },
    { twinClassFieldId: NAME, direction: "ASC" },
    { twinClassFieldId: CREATED, direction: "ASC" },
  ], taskSearch({ statusIdList: [STATUS_SMALL] })));
  expectError("memberSorts max 2 is 400", threeSorts, 13701, "memberSorts");
  const missingField = await api("/private/twin/search/grouped/v1", { search: taskSearch(), groupSort: "KEY_ASC" });
  expectError("groupFieldId is required", missingField, 13701, "groupFieldId");
  const badSort = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "FOO"));
  add("unknown groupSort is 400", badSort.__http === 400, { http: badSort.__http, status: badSort.status, msg: badSort.msg });
  const nested = await api("/private/twin/search/grouped/v1", {
    search: taskSearch({ statusIdList: [STATUS_SMALL] }),
    groupFieldId: ASSIGNEE,
    groupFieldIds: [ASSIGNEE, STATUS],
    groupSort: "KEY_ASC",
  });
  const nestedGroups = nested.groups || [];
  const rejected = nested.__http === 400;
  const ignoredAndFlat = nested.__http === 200 && nested.status === 0 && nestedGroups.every((group) => Array.isArray(group.twins) && !group.groups);
  add("S12 nested grouping is not accepted", rejected || ignoredAndFlat, {
    http: nested.__http,
    status: nested.status,
    groups: nestedGroups.length,
    outcome: rejected ? "400" : ignoredAndFlat ? "extra field ignored, response stays one level" : "nested or unexpected",
    statusDetails: nested.statusDetails,
  });
  const emptySearch = await api("/private/twin/search/grouped/v1", { search: {}, groupFieldId: ASSIGNEE, groupSort: "KEY_ASC" });
  add("empty search over the domain is refused instead of a cut tree", emptySearch.__http === 400 && emptySearch.status === 11606 && String(emptySearch.statusDetails || "").includes("5000"), {
    http: emptySearch.__http,
    status: emptySearch.status,
    statusDetails: emptySearch.statusDetails,
    groups: emptySearch.groups?.length,
  });
} catch (error) {
  add("validation scenarios threw", false, String(error));
}

try {
  const oneId = twins[0].id;
  const hiddenMode = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "KEY_ASC", undefined, { twinIdList: [oneId] }), "lazyRelation=false");
  const shown = await api("/private/twin/search/grouped/v1", req(ASSIGNEE, "KEY_ASC", undefined, { twinIdList: [oneId] }), "showTwinMode=DETAILED&lazyRelation=false");
  const hiddenTwin = hiddenMode.groups?.[0]?.twins?.[0] || {};
  const shownTwin = shown.groups?.[0]?.twins?.[0] || {};
  add("without showTwinMode the leaf has no id; DETAILED returns the twin", hiddenMode.__http === 200 && !hiddenTwin.id && shownTwin.id === oneId && shownTwin.twinClassId === TASK, {
    hiddenKeys: Object.keys(hiddenTwin),
    shownId: shownTwin.id,
    shownClass: shownTwin.twinClassId,
  });
} catch (error) {
  add("show mode threw", false, String(error));
}

checks.push({
  name: "multiple-select data list in this domain",
  ok: null,
  detail: { multipleFields, skipped: multipleFields === 0, note: "no multiple=true field, so a twin in several options was not exercised" },
});
console.log("SKIP multiple-select data list in this domain");

const duplicateNames = [...userNames.values()].filter((name, index, all) => name != null && all.indexOf(name) !== index);
checks.push({
  name: "assignee display names that tie",
  ok: duplicateNames.length > 0 ? checks.find((item) => item.name.startsWith("S1 "))?.ok ?? null : null,
  detail: duplicateNames.length
    ? { duplicates: [...new Set(duplicateNames)], coveredBy: "S1 order includes the id tie-break" }
    : { duplicates: 0, note: "no shared assignee name, id tie-break was not observable" },
});
console.log((duplicateNames.length ? "INFO " : "SKIP ") + "assignee display names that tie");

const summary = {
  ranAt: new Date().toISOString(),
  tasks: twins.length,
  passed: checks.filter((item) => item.ok === true).length,
  failed: checks.filter((item) => item.ok === false).length,
  skipped: checks.filter((item) => item.ok == null).length,
  checks,
};
writeFileSync(join(OUT, "checks.json"), JSON.stringify(summary, null, 2));
console.log(`DONE passed=${summary.passed} failed=${summary.failed} skipped=${summary.skipped}`);
if (summary.failed) process.exitCode = 1;
