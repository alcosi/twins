\# Twins REST API

Twins REST API is divided into two main zones:

* `/public/` — for unauthenticated requests. For example, retrieving publicly available lists required by the frontend during user registration or for displaying landing pages.
* `/private/` — for authenticated requests.

# API Versioning

There are two main approaches to API versioning:

## Total API Versioning

The version is defined globally for all endpoints.

In this approach, the version is specified before the endpoint path:

```text
/v1/private/twin/search/{searchId}
```

## Endpoint Versioning

The version is defined separately for each specific endpoint.

In this approach, the version is specified after the endpoint path:

```text
/private/twin/search/{searchId}/v1
```

This means endpoint versions are independent from each other: one endpoint may exist in `v1`, `v2`, and `v3`, while another may exist only in `v1`.

Twins uses the second approach because it provides significantly greater flexibility when extending the API.

# API Types

All Twins API endpoints can be conditionally divided into two types:

* `classic-endpoint` — traditional REST endpoints implemented according to common REST best practices.
* `smart-endpoint` — flexible endpoints designed with support for `lazyRelations` and `showModes`.

# Smart Endpoints

If you inspect the DTO schema of a typical Twins response, you may notice a high level of nesting. Smart endpoints provide mechanisms to optimize both response size and processing speed.

The two main technologies used for this are:

* `lazyRelations`
* `showModes`

These mechanisms influence several important response characteristics:

* Response generation speed
* Amount of displayed information

The principle is straightforward: the less information is requested, the faster the request execution becomes.

This happens because DTO fields may themselves contain nested objects, require additional database queries, and trigger additional processing logic, all of which increase response generation time.

By choosing appropriate `showModes`, request execution can be significantly optimized.

A single REST request may contain multiple `showMode` parameters for different DTO types.

# Lazy Relations

Technically, `lazyRelation` is an HTTP query parameter that accepts two values:

## `true` (currently deprecated)

All related DTOs are included hierarchically in the response body.

This mode is convenient for visual inspection of the response but can dramatically increase response size due to duplicated data (for example, in Twin search responses).

### Example

Twin search response DTO with `lazyRelation=true`

## `false`

All related objects are moved into a separate response section called `relatedObjects`.

The main response body contains only UUID references to these objects, allowing the frontend to easily retrieve them from `relatedObjects`.

This mode significantly reduces response size due to uniqueness guarantees inside `relatedObjects`, while preserving the same level of information and frontend usability.

### Example

Twin search response DTO with `lazyRelation=false`

# Show Modes

Technically, `showModes` are a set of HTTP query parameters that allow the same API endpoints to return responses with different levels of detail.

In some ways, `showModes` solve a problem similar to GraphQL, where the frontend specifies what data it needs in the response. However, there are two key differences:

* `showMode` operates with field presets (groups of fields), not individual fields.
* The DTO architecture supports database loading optimization, not only response filtering.

## Show Mode Categories

All show modes can be conditionally divided into two types:

### Detail Modes

These modes increase the amount of displayed information.

Instead of returning only the object identifier, additional fields are included depending on the selected mode.

#### `HIDE`

Only the object identifier is returned.

#### `SHORT`

A minimal set of fields containing the most important information is returned.

#### `DETAILED`

All available fields are returned.

## Restriction Modes

These modes filter information according to predefined criteria.

# Example

If `TwinDTO` contains the fields:

```text
name, status, assignee, description, twinClass
```

then the following `showTwinMode` values may exist:

* `HIDE` — return only object identifiers
* `SHORT` — return only `name`, `status`, and `assignee`
* `DETAILED` — return all fields

More detailed information about existing `showModes` is described separately.

# Show Modes Propagation

Propagation of `showMode` through the response tree is important for optimizing request processing performance.

Different branches of the response tree may contain similar structures. For example, in a `TwinView` request, `StatusDTO` may appear both:

* directly inside `TwinDTO`
* deeper in the hierarchy, for example:
  `TwinDTO → TransitionDTO → StatusDTO`

## Current Implementation: Global Propagation

Currently, Twins uses global propagation.

This means that `showStatusMode` provided in request parameters determines the rendering mode for `StatusDTO` everywhere in the response tree, including inside both `TwinDTO` and `TransitionDTO`.

### Main Drawback

Clients may receive unnecessary data.

# Possible Solutions

## Path Propagation

A mechanism where the request explicitly specifies `showMode` for every branch of the response tree.

### Advantages

* Maximum precision for configuring any part of the response.

### Disadvantages

* Complex request construction because it requires passing a map between response-tree paths and corresponding `showMode` values.
* Impossible to use with `GET` requests.

## Pointer Propagation

A mechanism where `showMode` targets are identified using short parent-child pointers.

For the example above, this could look like:

```text
showTwinStatusMode
showTransitionStatusMode
```

### Advantages

* Request structure remains unchanged.
* Clients simply receive additional `showMode` parameters.
* Significantly more explicit targeting of `showMode`.
* Allows decoupling child-object conversion logic from parent-object `showMode`.

For example:

```text
showTwinMode
showTwinTransitionMode
```

become independent, meaning transitions can be requested even when:

```text
showTwinMode=SHORT
```

### Disadvantages

* Some ambiguity still remains in deeply nested trees with repeating structures.
* Parent-child pointers may still be non-unique within a large response tree.

# DTO

## Naming Convention

see dto_code_convention.md