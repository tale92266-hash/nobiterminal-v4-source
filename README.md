# NobiTerminal v4 "Nobita Edition" — Source Tree

This repo is the **complete, ready-to-build source tree of the NobiTerminal v4
"Nobita Edition" Android app** — exactly the tree that GitHub Actions run
**39952199982** compiled to produce the shipped v4 APK
(`com.nobiterminal`, signed with the v1–v4 testkey).

> The original upstream Termux README is kept at
> [README.upstream.md](README.upstream.md).

## Provenance

- Built by the generator repo `tale92266-hash/nobiterminal` @ commit `508b08d5`
  (workflow inputs: name=`com.nobiterminal`, type=`f-droid`,
  architectures=`aarch64`, plugin=`nobiterminal`, addons off).
- Base: upstream `termux/termux-app` @ `8629e63`, with the
  `termux-am-library` module vendored in-tree (as the generator does).
- Transformations applied with the generator's own scripts, in CI order:
  plugin app-patches (`nobiterminal-app-name`, `-drawer-default`,
  `-neon-default`, `-settings-suite`, `-tabs-bar`), then the base f-droid
  app-patches, then the `com.termux` → `com.nobiterminal` rename + folder
  migration.

## What's inside

- The full patched app: session **tabs bar** (`NobiTabsController`:
  tap = switch, + = new, × = close, long-press drag = rearrange),
  **DRAWER (☰)** as the built-in extra-keys default, the NobiTerminal
  **Settings suite** (Backup / Restore / Dev Ready / Terminal Theme /
  Extra Keys Bar), and the **Nobi Neon** theme as the fresh-install default.
- `app/src/main/assets/bootstrap-aarch64.tar.xz` and `app/src/main/assets/xz-aarch64/`
  — the bootstrap **taken from the shipped v4 APK itself**, so no bootstrap
  build is needed. It carries the NOBITA startup banner (in `etc/motd`)
  and the time-aware greeting (`etc/termux-login.sh`).

## Building

- JDK **17**; use the included **Gradle wrapper** (Gradle **9.2.1**,
  `gradle/wrapper/gradle-wrapper.jar` is present). AGP is **8.13.2** —
  older Gradle versions (e.g. the Gradle 7.x bundled in some on-device
  IDEs) **cannot** run it; the wrapper must be used.
- Android SDK with **platform 36** (`compileSdkVersion=36`) and
  **NDK 29.0.14206865** (pinned in `gradle.properties`).
- Build the app module (`assembleDebug`). Debug builds are signed with
  `app/testkey_untrusted.jks` — the same testkey as the shipped v1–v4 APKs,
  so a debug build installs **over** the installed app, keeping its data.
- Expect a **long first build**: the same tree took ~71 minutes on a
  GitHub-hosted runner, and the first run downloads several GB of
  dependencies. On a phone (e.g. in ACS) it will be slower — let it finish.

## Notes

- This is a snapshot for building/experimenting; the live app continues to
  be produced by the generator repo above.
