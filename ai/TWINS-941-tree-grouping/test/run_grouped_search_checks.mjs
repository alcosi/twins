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
const TASK = "7c027b60-0f6c-445c-9889-8ee3855d2c59";
const ASSIGNEE = "00000000-0000-0000-0011-000000000007";
const STATUS = "00000000-0000-0000-0011-000000000010";
const CREATED = "00000000-0000-0000-0011-000000000011";
const NAME = "00000000-0000-0000-0011-000000000003";
const CLASS = "00000000-0000-0000-0011-000000000013";
const WORK = "95f2c8ca-8b79-4971-9726-270a11c7d031";
const BOOL = "19e3f226-5249-46eb-8d01-e9a054494715";
const USER_FIELD = "27af06b8-f8c9-4603-bc40-f4f6259630c0";
const LINK = "60f013ff-e7cc-41c9-9d5e-af5e4b2b6586";
const LINK2 = "d9f3041a-c631-487d-9e76-044fc81378d4";
const STATUS_MID = "c432b42e-0dfe-4c9d-8c21-5a54aa7b1257";
const SHOW = [
  "showTwinMode=DETAILED",
  "showTwin2UserMode=SHORT",
  "showTwin2StatusMode=SHORT",
  "lazyRelation=false",
].join("&");
const SHOW_OPTIONS = SHOW + "&showDataListOptionMode=SHORT&showTwinField2DataListOptionMode=SHORT";

const checks = [];
function add(name, ok, detail) {
  checks.push({ name, ok: Boolean(ok), detail });
  const text = detail == null ? "" : " — " + clip(detail);
  console.log((ok ? "PASS " : "FAIL ") + name + text);
}
function clip(value) {
  const text = typeof value === "string" ? value : JSON.stringify(value);
  return text.length > 600 ? text.slice(0, 600) + "…" : text;
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
async function api(body, query = SHOW, timeoutMs = 180000) {
  const started = Date.now();
  const response = await fetch(BASE + "/private/twin/search/grouped/v1" + (query ? "?" + query : ""), {
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
    parsed = { raw: text.slice(0, 800) };
  }
  parsed.__http = response.status;
  parsed.__ms = Date.now() - started;
  console.log(`  ${response.status} ${parsed.__ms}ms groups=${parsed.groups?.length ?? "-"} status=${parsed.status}`);
  return parsed;
}

const SIGN = 1n << 63n;
function uuidCompare(left, right) {
  const hex = (value) => value.replace(/-/g, "");
  const halves = (value) => {
    const raw = hex(value);
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
  return (left, right) => {
    if (left.key == null || right.key == null) return nullsLast(left.key, right.key);
    const compared = typeof left.key === "string" && typeof right.key === "string" ? uuidCompare(left.key, right.key) : compareText(String(left.key), String(right.key), true);
    return ascending ? compared : -compared;
  };
}
function nameOrder(names, ascending) {
  return (left, right) => {
    if (left.key == null || right.key == null) return nullsLast(left.key, right.key);
    const compared = compareText(names.get(left.key) ?? null, names.get(right.key) ?? null, ascending);
    if (compared !== 0) return compared;
    return uuidCompare(left.key, right.key);
  };
}
function byCount(ascending) {
  return (left, right) => {
    let compared = left.count - right.count;
    if (!ascending) compared = -compared;
    if (compared !== 0) return compared;
    return byKey(true)(left, right);
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
      node.twins.push(twin);
    }
    return;
  }
  node.ids.add(twin.id);
  let keys = levels[depth];
  if (!keys || keys.length === 0) keys = [null];
  const used = new Set();
  for (const key of keys) {
    const token = key == null ? "\0" : JSON.stringify(key);
    if (used.has(token)) continue;
    used.add(token);
    let child = node.children.get(token);
    if (!child) {
      child = { key: key ?? null, children: new Map(), ids: new Set(), twins: [] };
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
    ids: last ? group.twins.map((twin) => twin.id) : undefined,
  }));
}

function has(object, key) {
  return Object.prototype.hasOwnProperty.call(object, key);
}
function norm(value) {
  return value == null ? null : value;
}
function keyOf(group, type) {
  return group.key && group.key.type === type ? norm(group.key.value) : undefined;
}
const readers = {
  status: (group) => keyOf(group, "STATUS"),
  assignee: (group) => keyOf(group, "USER"),
  link: (group) => (group.key && has(group.key, "linkId") ? norm(group.key.value) : undefined),
  work: (group) => keyOf(group, "DATA_LIST_OPTION"),
  bool: (group) => keyOf(group, "BOOLEAN"),
};
function actual(groups, kinds) {
  return (groups || []).map((group) => ({
    key: readers[kinds[0]](group),
    present: readers[kinds[0]](group) !== undefined || (kinds[0] === "link" ? Boolean(group.key && has(group.key, "linkId")) : false),
    linkId: group.key ? group.key.linkId : undefined,
    count: Number(group.count),
    hasTwins: has(group, "twinIdList"),
    hasGroups: has(group, "groups"),
    groups: group.groups ? actual(group.groups, kinds.slice(1)) : undefined,
    ids: group.twinIdList || [],
    rawKeys: Object.keys(group),
  }));
}
function sameIds(left, right) {
  if (!left || !right || left.length !== right.length) return false;
  for (let i = 0; i < left.length; i++) if (left[i] !== right[i]) return false;
  return true;
}
function compareTree(got, want, problems, path) {
  if (got.length !== want.length) {
    problems.push(`${path} groups ${got.length} != ${want.length}`);
    return;
  }
  for (let i = 0; i < want.length; i++) {
    const here = `${path}[${i}]`;
    if (JSON.stringify(got[i].key) !== JSON.stringify(want[i].key)) {
      problems.push(`${here} key ${JSON.stringify(got[i].key)} != ${JSON.stringify(want[i].key)}`);
      continue;
    }
    if (got[i].count !== want[i].count) problems.push(`${here} count ${got[i].count} != ${want[i].count}`);
    if (want[i].groups) {
      if (got[i].hasTwins) problems.push(`${here} twins on a non-leaf`);
      if (!got[i].hasGroups) problems.push(`${here} missing nested groups`);
      else compareTree(got[i].groups, want[i].groups, problems, here);
    } else if (!sameIds(got[i].ids, want[i].ids)) {
      const at = (got[i].ids || []).findIndex((id, index) => id !== want[i].ids[index]);
      problems.push(`${here} leaves ${got[i].ids?.length} != ${want[i].ids.length} @${at} ${got[i].ids?.[at]} != ${want[i].ids[at]}`);
    } else if (got[i].hasGroups) {
      problems.push(`${here} nested groups on the leaf level`);
    }
    if (problems.length > 8) return;
  }
}
function rootSum(groups) {
  return groups.reduce((sum, group) => sum + group.count, 0);
}
function allLeafIds(groups) {
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

const twins = psqlJson(`
SELECT coalesce(json_agg(row_to_json(x)), '[]'::json)::text FROM (
  SELECT t.id::text AS id,
         t.twin_status_id::text AS status,
         t.assigner_user_id::text AS assignee,
         to_char(t.created_at, 'YYYY-MM-DD"T"HH24:MI:SS.US') AS created_at,
         row_number() OVER (ORDER BY t.created_at DESC, t.id ASC) AS created_rank,
         row_number() OVER (ORDER BY t.name ASC, t.id ASC) AS name_rank
  FROM twin t
  WHERE t.twin_class_id = '${TASK}'
) x
`);
const users = new Map(Object.entries(psqlJson(`
SELECT coalesce(json_object_agg(u.id::text, u.name), '{}'::json)::text
FROM "user" u
WHERE u.id IN (SELECT assigner_user_id FROM twin WHERE twin_class_id = '${TASK}' AND assigner_user_id IS NOT NULL)
`) || {}));
const linkRows = psqlJson(`
SELECT coalesce(json_agg(json_build_object('src', src_twin_id, 'dst', dst_twin_id)), '[]'::json)::text
FROM twin_link WHERE link_id = '${LINK}'
`);
const dstNames = new Map(Object.entries(psqlJson(`
SELECT coalesce(json_object_agg(h.id::text, to_jsonb(h.name)), '{}'::json)::text
FROM twin h
WHERE h.id IN (SELECT DISTINCT dst_twin_id FROM twin_link WHERE link_id = '${LINK}')
`) || {}).map(([id, name]) => [id, name]));
const workRows = psqlJson(`
SELECT coalesce(json_agg(json_build_object('twinId', twin_id, 'optionId', data_list_option_id)), '[]'::json)::text
FROM twin_field_data_list WHERE twin_class_field_id = '${WORK}'
`);
const boolRows = psqlJson(`
SELECT coalesce(json_agg(json_build_object('twinId', twin_id, 'value', value)), '[]'::json)::text
FROM twin_field_boolean WHERE twin_class_field_id = '${BOOL}'
`);

const linksBySrc = new Map();
for (const row of linkRows) {
  if (!linksBySrc.has(row.src)) linksBySrc.set(row.src, []);
  linksBySrc.get(row.src).push(row.dst);
}
const workByTwin = new Map();
for (const row of workRows) workByTwin.set(row.twinId, row.optionId ?? null);
const boolByTwin = new Map();
for (const row of boolRows) {
  if (!boolByTwin.has(row.twinId)) boolByTwin.set(row.twinId, []);
  boolByTwin.get(row.twinId).push(row.value);
}
twins.sort((left, right) => left.created_rank - right.created_rank);
const byCreated = twins;
const byName = [...twins].sort((left, right) => left.name_rank - right.name_rank);
console.log(`DB tasks=${twins.length} linkRows=${linkRows.length} linkedTasks=${linksBySrc.size} users=${users.size}`);

function taskLevels(twin) {
  const dst = linksBySrc.get(twin.id);
  return [
    [twin.status],
    [twin.assignee ?? null],
    dst && dst.length ? [...new Set(dst)] : [null],
  ];
}
const nameSort = nameOrder(users, true);
const linkSort = nameOrder(dstNames, true);
const keySort = byKey(true);

function taskBody(extraSearch, criteria, memberSorts) {
  const body = {
    search: { twinClassIdList: [TASK], ...extraSearch },
    criteria,
  };
  if (memberSorts) body.memberSorts = memberSorts;
  return body;
}
const three = [
  { type: "FIELD", fieldId: STATUS, sort: "KEY_ASC" },
  { type: "FIELD", fieldId: ASSIGNEE, sort: "NAME_ASC" },
  { type: "LINK", linkId: LINK, nodeEnd: "DST", sort: "NAME_ASC" },
];

try {
  const body = await api(taskBody(undefined, three, [{ twinClassFieldId: CREATED, direction: "DESC" }]));
  const want = build(byCreated, taskLevels, [keySort, nameSort, linkSort]);
  const got = actual(body.groups, ["status", "assignee", "link"]);
  const problems = [];
  if (body.__http !== 200 || body.status !== 0) problems.push({ http: body.__http, status: body.status, statusDetails: body.statusDetails });
  if ("pagination" in body) problems.push("pagination present");
  if (problems.length === 0) compareTree(got, want, problems, "root");
  const sum = rootSum(got);
  if (sum !== twins.length) problems.push(`root count sum ${sum} != ${twins.length}`);
  const leafIds = new Set(allLeafIds(got));
  if (leafIds.size !== twins.length) problems.push(`unique leaves ${leafIds.size}`);
  const wrongClass = (body.groups || []).length && leafIds.size && false;
  add("S1 status, assignee, link tree matches the flat TASK selection", problems.length === 0 && !wrongClass, problems.length ? problems : {
    roots: got.length,
    sum,
    leaves: leafIds.size,
    ms: body.__ms,
  });

  let nullAssigneeLast = true;
  let nullLinkSeen = false;
  let multi = null;
  const walk = (nodes, depth, statusKey, assigneeKey) => {
    for (const node of nodes) {
      if (depth === 1 && node.key == null && nodes[nodes.length - 1] !== node) nullAssigneeLast = false;
      if (depth === 2 && node.key == null) nullLinkSeen = true;
      if (depth === 2 && node.ids && node.ids.length) {
        for (const id of node.ids) {
          const dst = linksBySrc.get(id);
          if (dst && new Set(dst).size > 1) multi = multi || { id, statusKey, assigneeKey, dst: [...new Set(dst)] };
        }
      }
      if (node.groups) walk(node.groups, depth + 1, depth === 0 ? node.key : statusKey, depth === 1 ? node.key : assigneeKey);
    }
  };
  walk(got, 0);
  add("S2 empty assignee is last under its status and a task without the link stays in the tree", nullAssigneeLast && nullLinkSeen && sum === twins.length, {
    nullAssigneeLast,
    nullLinkSeen,
  });

  if (multi) {
    const statusNode = got.find((node) => node.key === multi.statusKey);
    const assigneeNode = statusNode?.groups.find((node) => JSON.stringify(node.key) === JSON.stringify(multi.assigneeKey));
    const linkNodes = (assigneeNode?.groups || []).filter((node) => (node.ids || []).includes(multi.id) || node.groups?.some((child) => (child.ids || []).includes(multi.id)));
    const hosting = (assigneeNode?.groups || []).filter((node) => (node.ids || []).includes(multi.id));
    const parentCount = assigneeNode?.count;
    const childSum = (assigneeNode?.groups || []).reduce((total, node) => total + node.count, 0);
    const onceEach = hosting.every((node) => node.ids.filter((id) => id === multi.id).length === 1);
    add("S3 one task with several dst is one leaf in each link node and one in the assignee count", hosting.length === new Set(multi.dst).size && onceEach && childSum > parentCount, {
      id: multi.id,
      dst: multi.dst.length,
      hosting: hosting.length,
      assigneeCount: parentCount,
      linkCountSum: childSum,
    });
  } else {
    add("S3 one task with several dst is one leaf in each link node and one in the assignee count", false, "no multi-dst task in the response");
  }

  const sample = got[0]?.groups?.find((node) => node.key == null) || got[0]?.groups?.[got[0].groups.length - 1];
  writeFileSync(join(OUT, "raw", "s2-null-assignee-group.json"), JSON.stringify({
    status: got.find((node) => node.groups?.some((child) => child.key == null))?.key,
    nullAssignee: sample ? { key: sample.key, count: sample.count, childKeys: (sample.groups || []).slice(0, 3).map((child) => child.key) } : null,
  }, null, 2));

  const filteredTwins = byCreated.filter((twin) => twin.status === STATUS_MID);
  const filtered = await api(taskBody({ statusIdList: [STATUS_MID] }, three, [{ twinClassFieldId: CREATED, direction: "DESC" }]));
  const filteredWant = build(filteredTwins, taskLevels, [keySort, nameSort, linkSort]);
  const filteredGot = actual(filtered.groups, ["status", "assignee", "link"]);
  const filterProblems = [];
  if (filtered.__http !== 200) filterProblems.push(filtered.statusDetails);
  else compareTree(filteredGot, filteredWant, filterProblems, "filtered");
  add("S4 a status filter leaves one root and recounts assignee and link", filterProblems.length === 0 && filteredGot.length === 1 && filteredGot[0]?.key === STATUS_MID, filterProblems.length ? filterProblems : {
    roots: filteredGot.length,
    count: filteredGot[0]?.count,
  });

  const one = await api(taskBody(undefined, [{ type: "FIELD", fieldId: ASSIGNEE, sort: "NAME_ASC" }]));
  const oneWant = build(byCreated, (twin) => [[twin.assignee ?? null]], [nameSort]);
  const oneGot = actual(one.groups, ["assignee"]);
  const oneProblems = [];
  if (one.__http !== 200) oneProblems.push(one.statusDetails);
  else compareTree(oneGot, oneWant, oneProblems, "one");
  const oneLeaves = new Set(allLeafIds(oneGot));
  const oneHasTwins = oneGot.every((group) => group.hasTwins && !group.hasGroups);
  add("S5 one criterion puts twins on the root", oneProblems.length === 0 && oneHasTwins && oneLeaves.size === twins.length, oneProblems.length ? oneProblems : { groups: oneGot.length, leaves: oneLeaves.size });

  const two = await api(taskBody(undefined, [
    { type: "FIELD", fieldId: STATUS, sort: "KEY_ASC" },
    { type: "FIELD", fieldId: ASSIGNEE, sort: "NAME_ASC" },
  ]));
  const twoWant = build(byCreated, (twin) => [[twin.status], [twin.assignee ?? null]], [keySort, nameSort]);
  const twoGot = actual(two.groups, ["status", "assignee"]);
  const twoProblems = [];
  if (two.__http !== 200) twoProblems.push(two.statusDetails);
  else compareTree(twoGot, twoWant, twoProblems, "two");
  const twinsOnSecond = twoGot.every((group) => group.hasGroups && !group.hasTwins && group.groups.every((child) => child.hasTwins && !child.hasGroups));
  add("S5 two criteria put twins on the second level", twoProblems.length === 0 && twinsOnSecond && new Set(allLeafIds(twoGot)).size === twins.length, twoProblems.length ? twoProblems : { roots: twoGot.length });

  const counted = await api(taskBody(undefined, [
    { type: "FIELD", fieldId: STATUS, sort: "COUNT_DESC" },
    { type: "FIELD", fieldId: ASSIGNEE, sort: "NAME_ASC" },
    { type: "LINK", linkId: LINK, nodeEnd: "DST", sort: "NAME_ASC" },
  ], [{ twinClassFieldId: NAME, direction: "ASC" }]));
  const countedWant = build(byName, taskLevels, [byCount(false), nameSort, linkSort]);
  const countedGot = actual(counted.groups, ["status", "assignee", "link"]);
  const countProblems = [];
  if (counted.__http !== 200) countProblems.push(counted.statusDetails);
  else compareTree(countedGot, countedWant, countProblems, "count");
  const firstIsLargest = countedGot.length > 1 && countedGot[0].count >= countedGot[1].count;
  add("S12 largest status is first and leaves under the link follow the name order", countProblems.length === 0 && firstIsLargest, countProblems.length ? countProblems : {
    first: countedGot[0]?.count,
    second: countedGot[1]?.count,
  });
} catch (error) {
  add("tree scenarios threw", false, String(error.stack || error));
}

try {
  const workBody = await api(taskBody(undefined, [
    { type: "FIELD", fieldId: STATUS, sort: "KEY_ASC" },
    { type: "FIELD", fieldId: WORK, sort: "KEY_ASC" },
  ]), SHOW_OPTIONS);
  const workWant = build(byCreated, (twin) => [[twin.status], [workByTwin.has(twin.id) ? workByTwin.get(twin.id) : null]], [keySort, keySort]);
  const workGot = actual(workBody.groups, ["status", "work"]);
  const workProblems = [];
  if (workBody.__http !== 200) workProblems.push({ http: workBody.__http, statusDetails: workBody.statusDetails });
  else compareTree(workGot, workWant, workProblems, "work");
  const optionIds = new Set();
  for (const statusNode of workGot) {
    for (const optionNode of statusNode.groups || []) {
      if (optionNode.key) optionIds.add(optionNode.key);
    }
  }
  optionIds.delete(undefined);
  const map = workBody.relatedObjects?.dataListsOptionMap || {};
  const missing = [...optionIds].filter((id) => id && !map[id]);
  add("S11 workType is a field node and its options are related objects", workProblems.length === 0 && missing.length === 0, workProblems.length ? workProblems : { missing, options: Object.keys(map).length });

  const boolBody = await api(taskBody(undefined, [{ type: "FIELD", fieldId: BOOL, sort: "KEY_ASC" }]));
  const boolWant = build(byCreated, (twin) => [boolByTwin.get(twin.id) || [null]], [byKey(true)]);
  const boolGot = actual(boolBody.groups, ["bool"]);
  const boolProblems = [];
  if (boolBody.__http !== 200) boolProblems.push(boolBody.statusDetails);
  else compareTree(boolGot, boolWant, boolProblems, "bool");
  const nullBool = (boolBody.groups || []).find((group) => group.key && group.key.type === "BOOLEAN" && group.key.value === null);
  add("boolean field keeps true, false and a null key", boolProblems.length === 0 && Boolean(nullBool), boolProblems.length ? boolProblems : {
    keys: boolGot.map((group) => group.key),
  });
} catch (error) {
  add("dynamic field scenarios threw", false, String(error.stack || error));
}

function expectError(name, body, code, text) {
  const details = `${body.statusDetails || ""} ${body.msg || ""}`;
  const ok = body.__http === 400 && (!code || body.status === code) && (!text || details.toLowerCase().includes(text.toLowerCase()));
  add(name, ok, { http: body.__http, status: body.status, msg: body.msg, statusDetails: body.statusDetails });
}

try {
  expectError("S6 four criteria are rejected", await api(taskBody(undefined, [
    { type: "FIELD", fieldId: STATUS },
    { type: "FIELD", fieldId: ASSIGNEE },
    { type: "LINK", linkId: LINK, nodeEnd: "DST" },
    { type: "FIELD", fieldId: CLASS },
  ])), 13701, "criteria");
  expectError("S7 an empty class list is rejected", await api({ search: { twinClassIdList: [] }, criteria: [{ type: "FIELD", fieldId: ASSIGNEE }] }), 11604, "exactly one");
  expectError("S7 two classes are rejected", await api({
    search: { twinClassIdList: [TASK, "458c6d7d-99c8-4d87-89c6-2f72d0f5d673"] },
    criteria: [{ type: "FIELD", fieldId: ASSIGNEE }],
  }), 11604, "exactly one");
  expectError("S8 name is not groupable", await api(taskBody(undefined, [{ type: "FIELD", fieldId: NAME, sort: "KEY_ASC" }])), 11604, "not groupable");
  expectError("S8 createdAt is not groupable", await api(taskBody(undefined, [{ type: "FIELD", fieldId: CREATED, sort: "KEY_ASC" }])), 11604, "not groupable");
  expectError("S8 a user field is not countable", await api(taskBody(undefined, [{ type: "FIELD", fieldId: USER_FIELD, sort: "KEY_ASC" }])), 11604, "not countable");
  expectError("S8 NAME_ASC on status is rejected", await api(taskBody(undefined, [{ type: "FIELD", fieldId: STATUS, sort: "NAME_ASC" }])), 11604, "NAME sort");
  expectError("S9 the same field twice is rejected", await api(taskBody(undefined, [
    { type: "FIELD", fieldId: STATUS },
    { type: "FIELD", fieldId: STATUS },
  ])), 11604, "duplicate");
  expectError("S9 the same link and end twice is rejected", await api(taskBody(undefined, [
    { type: "LINK", linkId: LINK, nodeEnd: "DST" },
    { type: "LINK", linkId: LINK, nodeEnd: "DST" },
  ])), 11604, "duplicate");
  const twoLinks = await api(taskBody({ statusIdList: [STATUS_MID] }, [
    { type: "LINK", linkId: LINK, nodeEnd: "DST", sort: "KEY_ASC" },
    { type: "LINK", linkId: LINK2, nodeEnd: "DST", sort: "KEY_ASC" },
  ]));
  add("S9 two different links are a normal tree", twoLinks.__http === 200 && twoLinks.status === 0 && (twoLinks.groups || []).every((group) => Array.isArray(group.groups) && !group.twinIdList), {
    http: twoLinks.__http,
    status: twoLinks.status,
    roots: twoLinks.groups?.length,
    statusDetails: twoLinks.statusDetails,
  });
  const bothEnds = await api(taskBody({ statusIdList: [STATUS_MID] }, [
    { type: "LINK", linkId: LINK, nodeEnd: "DST", sort: "KEY_ASC" },
    { type: "LINK", linkId: LINK, nodeEnd: "SRC", sort: "KEY_ASC" },
  ]));
  add("the same link with DST and SRC is accepted", bothEnds.__http === 200 && bothEnds.status === 0, {
    http: bothEnds.__http,
    status: bothEnds.status,
    statusDetails: bothEnds.statusDetails,
  });
  const foreign = await api({
    search: { twinClassIdList: [TASK], twinIdList: ["cf850c7e-d8cd-498d-bb97-47101fa86517"] },
    criteria: [{ type: "FIELD", fieldId: ASSIGNEE, sort: "KEY_ASC" }],
  });
  add("S10 a twin from another domain is absent and the response stays 200", foreign.__http === 200 && foreign.status === 0 && (foreign.groups || []).length === 0, {
    http: foreign.__http,
    groups: foreign.groups?.length,
    status: foreign.status,
  });
} catch (error) {
  add("validation scenarios threw", false, String(error.stack || error));
}

try {
  const bigClass = psql(`
    SELECT t.twin_class_id::text FROM twin t
    JOIN twin_class c ON c.id = t.twin_class_id
    WHERE c.domain_id = 'f67ad556-dd27-4871-9a00-16fb0e8a4102'
    GROUP BY t.twin_class_id
    ORDER BY count(*) DESC
    LIMIT 1
  `);
  const bigCount = Number(psql(`SELECT count(*) FROM twin WHERE twin_class_id = '${bigClass}'`));
  const big = await api({
    search: { twinClassIdList: [bigClass] },
    criteria: [{ type: "FIELD", fieldId: CLASS, sort: "KEY_ASC" }],
  }, "showTwinMode=SHORT&lazyRelation=false", 300000);
  const returned = (big.groups || []).reduce((sum, group) => sum + (group.twinIdList || []).length, 0);
  add("a class larger than 5000 is returned whole", big.__http === 200 && big.status === 0 && returned === bigCount, {
    http: big.__http,
    status: big.status,
    statusDetails: big.statusDetails,
    classId: bigClass,
    db: bigCount,
    returned,
    ms: big.__ms,
  });
} catch (error) {
  add("a class larger than 5000 is returned whole", false, String(error.stack || error).slice(0, 400));
}

const summary = {
  ranAt: new Date().toISOString(),
  tasks: twins.length,
  passed: checks.filter((item) => item.ok).length,
  failed: checks.filter((item) => !item.ok).length,
  checks,
};
writeFileSync(join(OUT, "checks.json"), JSON.stringify(summary, null, 2));
console.log(`DONE passed=${summary.passed} failed=${summary.failed}`);
if (summary.failed) process.exitCode = 1;
