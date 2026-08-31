---
name: worktree
description: Use when the user asks to create a git worktree, work on a branch in a separate checkout, or spin up an isolated workspace. Always places worktrees in a sibling ../worktrees folder next to the current repo checkout.
---

# Git worktrees

All worktrees live in a `worktrees/` folder that sits **next to** the current repo
checkout — never inside the checkout, never in `/tmp`, never anywhere else.

```
<project>/
  <checkout>/     <- primary checkout (the repo you are in)
  worktrees/      <- every worktree goes here, one subfolder per worktree
    <slug>/       <- flat: no base-branch (e.g. `main/`) folder in between
```

Resolve the location dynamically each time — do not hardcode paths.

## Creating a worktree

1. Ask the user how to name the branch and the worktree folder. These are
   typically named after a Jira story (e.g. `PROJ-123` or
   `PROJ-123-short-description`). Wait for their answer before continuing.

2. Find the **primary** checkout, even when you are already inside a worktree.
   `--git-common-dir` always points at the primary checkout's `.git`:

   ```bash
   git rev-parse --path-format=absolute --git-common-dir
   ```

   The primary checkout is that path with the trailing `/.git` removed; the
   worktrees directory is its sibling `../worktrees`.

3. Create the worktrees directory if missing:

   ```bash
   WT_ROOT="$(dirname "$(git rev-parse --path-format=absolute --git-common-dir)")/../worktrees"
   mkdir -p "$WT_ROOT"
   ```

4. Choose a `SLUG` for the subfolder from the name the user gave: the branch name
   with any `/` replaced by `-` (e.g. branch `feature/login-fix` -> folder
   `feature-login-fix`). The worktree goes directly in `$WT_ROOT/<SLUG>` — never
   nest it under a base-branch folder like `$WT_ROOT/main/<SLUG>`.

5. Create the worktree:

   - New branch:

     ```bash
     git worktree add -b <branch-name> "$WT_ROOT/<SLUG>"
     ```

   - Existing local or remote branch:

     ```bash
     git worktree add "$WT_ROOT/<SLUG>" <branch-name>
     ```

6. Report the absolute path of the new worktree. `cd` into it if the user wants to
   start working there.

## Rules

- Never create a worktree outside `../worktrees/`. If the user names a different
  location, place it in `../worktrees/` anyway and tell them where it went.
- Keep the layout flat: `../worktrees/<SLUG>`. Never insert a base-branch folder
  (e.g. `../worktrees/main/<SLUG>`).
- One subfolder per branch; reuse the existing folder if it is already a worktree
  for that branch.
- Respect any repo instructions about committing/pushing (e.g. CLAUDE.md).

## Listing and removing

```bash
git worktree list
git worktree remove "$WT_ROOT/<SLUG>"   # WT_ROOT as resolved in step 3
git worktree prune
```
