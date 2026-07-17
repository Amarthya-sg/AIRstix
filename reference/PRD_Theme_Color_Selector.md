# PRD: Theme Color Selector — Settings Screen

**Status:** Draft for review
**Owner:** Glaks
**Related:** `Theme.kt`, `PRD_Minimalist_Palette_System.md`, `settings_theme_circles.html` (interactive mockup)

## 1. Summary

Replace the current **Theme Color dropdown** in Settings → Display with a **grid of selectable color circles** — one per palette. Each circle renders all 5 of that palette's color roles (Background, Surface, Muted, Text, Accent) as curved wedges, so the user can identify and compare palettes visually instead of scrolling through a text list.

## 2. Problem

The existing dropdown (`Molten Amber ▾`) only surfaces one palette name at a time. To compare options, the user has to open the dropdown, read each name, and mentally recall what that palette looks like — there's no visual preview until it's actually applied. With 15 palettes now available (10 dark + 5 light), this gets slower and harder to scan.

## 3. Goals

- Show all palettes at once as compact, tappable color previews.
- Make the current selection unambiguous (clear selected state).
- Make each preview an accurate color fingerprint of the palette — not just its accent color.
- Keep the interaction lightweight: one tap to preview/select, no secondary confirmation step needed before Save.

## 4. Non-Goals

- Changing how the theme is actually stored/applied in `Theme.kt` — this PRD only covers the selector UI.
- Editing or creating custom palettes from this screen (still out of scope per the parent PRD).
- Removing the palette name entirely — it still needs to display as text somewhere for accessibility and confirmation.

## 5. Design

### 5.1 Circle anatomy
Each circle is divided into 5 equal wedges (72° each), one per color role, in this fixed order: **Background → Surface → Muted → Text → Accent**. Rather than meeting at a sharp point in the center (a standard pie chart), each wedge's inner edge is a small curved arc — visually closer to a rounded pinwheel/flower than a pie chart, which reads as friendlier and less "chart-like" for a settings control.

### 5.2 Selected state
The active palette's circle gets:
- A ring (accent-colored outline, offset from the circle so it doesn't distort the color wedges)
- A checkmark overlay, so selection is visible even for users who can't distinguish the ring color from the wedge colors

### 5.3 Selection confirmation
Below the circle grid, the currently selected palette's **name** is shown as text (with a small accent-colored dot) so the choice is confirmed in words, not just color — this matters for accessibility and for users who want to be certain before hitting Save.

### 5.4 Layout
Circles wrap in a flexible grid (no fixed column count), sized for comfortable tap targets (~64px), with even spacing. Dark and light palettes are not visually separated in the grid in this version — see Open Questions.

### 5.5 Background chip
Because the circle itself renders the palette's own Background/Surface/Text colors, a circle whose colors are mostly white or near-white can visually disappear against a light page theme (and the reverse for near-black colors on a dark page theme) — including the small curved center hole from Section 5.1, which has no fill of its own and otherwise shows raw page background through it.

To fix this, each circle sits inside a fixed neutral gray chip (a translucent gray disc, independent of whichever theme is currently active on the page). This guarantees:
- Every circle has a consistent, theme-independent contrast edge to sit against.
- The center hole always shows a visible neutral tone instead of blending into the page.
- The chip color never changes when the user switches themes, so scanning the grid stays predictable regardless of which palette is currently applied.

## 6. Interaction Flow

1. User opens Settings → Display.
2. User taps any circle in the Theme Color grid.
3. The selected circle updates immediately (ring + checkmark), and the palette name below updates.
4. *(Depending on implementation choice — see Open Questions)* either the whole app re-themes live as a preview, or the new theme only applies once Save is tapped.
5. User taps Save to persist, or Cancel/Reset to discard the change.

## 7. Acceptance Criteria

- All 15 palettes are visible in the Theme Color grid without needing to scroll horizontally.
- Tapping a circle updates the selected state within one frame (no visible lag).
- Exactly one circle can be selected at a time.
- The selected palette's name is always shown as text alongside the grid.
- Circle rendering works identically for dark and light palettes (no role is illegible in either).
- No circle's outer edge or center hole visually disappears into the page background, regardless of which theme is currently active.

## 8. Open Questions

- **Live preview vs. commit-on-Save**: should tapping a circle re-theme the whole Settings screen immediately (as in the mockup), or only stage the change until Save is pressed? Live preview is more delightful but risks feeling like the change already "took" before Save/Cancel is resolved.
- Should dark and light palettes be grouped into two visually separate rows/sections, or stay as one flat grid (current mockup)?
- Do we need a text label under each circle, or is the name-below-grid + tooltip-on-hover (current mockup) sufficient for discoverability, especially on first use?
- Should the grid remember scroll position / order, or should the current selection always sort to the front?

## 9. Reference

Interactive mockup: `settings_theme_circles.html` — full Settings screen with the circle-based Theme Color selector, live re-theming on tap, and all 15 palettes from `PRD_Minimalist_Palette_System.md`.
