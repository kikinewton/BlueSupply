# IdentifierUtil Improvement Design

**Goal:** Fix correctness bugs in `IdentifierUtil.idHandler` that produce variable-length reference codes, simplify the implementation, and clean up all 7 call sites.

**Date:** 2026-04-03

---

## Problems Being Solved

1. **Variable-length `dm` suffix** — day and month are concatenated without zero-padding. April 3 → `"34"` (2 chars), October 14 → `"1410"` (4 chars). Refs are not consistently formatted.
2. **Inconsistent department segment** — names ≤ 3 chars are prefixed with `"O"` (e.g. `"IT"` → `"OIT"`, but `"A"` → `"OA"` — only 2 chars, not 3).
3. **Verbose zero-padding loop** — 8 lines of string concatenation replaced by `String.format`.
4. **Dead `@NotBlank` annotation** — Bean Validation does not fire on static method parameters.
5. **Caller noise** — every call site wraps an integer count in `String.valueOf()` before passing it.

---

## Design

### Signature

```java
public static String idHandler(String prefix, String department, int id)
```

- `id` changes from `String` to `int`. All 7 call sites pass an integer count — this eliminates the `String.valueOf()` wrapping at each site.
- `@NotBlank` is removed (it was never enforced).

### Output Format

```
PREFIX-DEP-00000042-MMDD
```

All segments have fixed length:

| Segment | Length | Rule |
|---------|--------|------|
| `PREFIX` | 3 | First 3 chars of `prefix`, uppercased |
| `DEP` | 3 | First 3 chars of `department`, uppercased; right-padded with `'X'` if shorter than 3 |
| `id` | 8 | Zero-padded: `String.format("%08d", id)` |
| `dm` | 4 | `String.format("%02d%02d", day, month)` — day then month, each zero-padded to 2 digits |

**Example:** prefix=`"GRN"`, dept=`"STORES"`, id=`42`, date=April 3 → `GRN-STO-00000042-0304`

**Short dept example:** dept=`"IT"` → `"ITX"`, dept=`"A"` → `"AXX"`

### Implementation

```java
@UtilityClass
public class IdentifierUtil {

  public static String idHandler(String prefix, String department, int id) {
    String pre = prefix.substring(0, 3);
    String dep = String.format("%-3s", department.substring(0, Math.min(3, department.length())))
                       .replace(' ', 'X');
    LocalDate today = LocalDate.now();
    String dm = String.format("%02d%02d", today.getDayOfMonth(), today.getMonth().getValue());
    String paddedId = String.format("%08d", id);
    return (pre + "-" + dep + "-" + paddedId + "-" + dm).toUpperCase(Locale.ROOT);
  }
}
```

### Call-Site Changes (7 files)

Each change is a one-liner — drop `String.valueOf()`:

| File | Before | After |
|------|--------|-------|
| `GRNController.java:172` | `idHandler("GRN", "STORES", String.valueOf(count))` | `idHandler("GRN", "STORES", count)` |
| `FloatGRNService.java:57` | `idHandler("FLG", "STORES", String.valueOf(count()))` | `idHandler("FLG", "STORES", count())` |
| `LocalPurchaseOrderDraftService.java:195` | `idHandler("LPO", department, count)` | already `String` — change `count` type at call site |
| `PettyCashService.java:89` | `idHandler("PTC", ..., String.valueOf(refCount))` | `idHandler("PTC", ..., refCount)` |
| `QuotationService.java:88` | `idHandler("QUO", ..., String.valueOf(count))` | `idHandler("QUO", ..., count)` |
| `FloatOrderService.java:470` | `idHandler("FLT", ..., String.valueOf(...count()+1))` | `idHandler("FLT", ..., (int)(floatOrderRepository.count()+1))` |
| `RequestItemFactory.java:28` | `idHandler("RQI", ..., String.valueOf(refCount.get()))` | `idHandler("RQI", ..., refCount.get())` |

Note: `LocalPurchaseOrderDraftService` passes `count` which is already a `String` (check actual type at implementation time and cast/parse as needed).

---

## Testing

New `IdentifierUtilTest` (plain JUnit 5, no Spring context):

| Test | Verifies |
|------|----------|
| `normalDept_producesCorrectFormat` | Full output structure matches `PREFIX-DEP-00000042-MMDD` |
| `shortDept_rightPaddedWithX` | 2-char dept → 3-char segment ending in `X` |
| `singleCharDept_rightPaddedWithXX` | 1-char dept → 3-char segment ending in `XX` |
| `id_alwaysEightDigits` | id=1 → `"00000001"` in output |
| `dm_alwaysFourDigits` | Any date → 4-char dm segment (tested with a known date via `Clock` injection or output parsing) |
| `output_isUpperCase` | Lowercase prefix/dept inputs are uppercased |

---

## Out of Scope

- No `RefPrefix` enum — YAGNI; the 7 string literals are already well-contained.
- No change to the ref format itself (segment order, separator character).
- No database migration — refs are stored strings; existing stored refs are unaffected by this change (only new refs use the fixed format).
