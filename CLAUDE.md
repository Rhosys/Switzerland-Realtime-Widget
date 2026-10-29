# Switzerland Realtime Widget — Claude Code guidelines

Rules and conventions for every session.

---

## CI/CD — never use `sudo` to run application code

**Rule:** `sudo` is only permitted for infrastructure setup (package installation,
service start, writing root-owned config). Never use it to invoke `gradle`, `npm`,
`cargo`, `python`, or any test runner.

**Why it breaks:** CI runners install toolchains under the non-root user's home;
`sudo` switches to root which has a different `$HOME`, `$PATH`, and no access to
the user's Gradle or npm caches.

**Correct pattern — grant access instead of escalating:**

| Problem | Wrong | Right |
|:--------|:------|:------|
| Process needs a device file | `sudo myapp` | `sudo chmod a+rw /dev/ttyX` in setup; run `myapp` as user |
| Process needs a privileged port | `sudo server` | `sudo setcap cap_net_bind_service+ep ./server` in setup; run as user |
| Process needs group membership | `sudo myapp` | `sudo usermod -a -G group $USER` + `newgrp group` |

---

## Local development — run on the emulator

```bash
npm run start            # debug variant: boots emulator, builds, installs, launches, streams crash logs
npm run start:release    # release variant: same loop on the R8/ProGuard build — catches stripping crashes
```

`scripts/dev.sh` is the single orchestrator: runs `setup.sh` if the SDK is missing,
creates the shared `WorkspaceAVD` (android-35, pixel_7) if absent, boots it, then
gradle install + launch. All workspace Android apps share one `WorkspaceAVD` and
one system image — do not give this app its own AVD name.

Emulator-only helpers: `npm run setup`, `npm run emulator:create|start|delete`.
KVM is required (Linux). Troubleshooting lives in `scripts/setup.sh`.

---

## Repository layout

```
app/src/main/java/ch/rhosys/realtime/
  RealtimeApp.kt                       ← @HiltAndroidApp + PostHog + HiltWorkerFactory
  MainActivity.kt                      ← Compose entry point
  data/local/location/LocationProvider.kt ← app-lifetime location StateFlow
  di/
    NetworkModule.kt                   ← OkHttp, Json, Retrofit (api.open-meteo.com)
    PreferencesModule.kt               ← DataStore<Preferences>
  ui/
    common/AppAlertDialog.kt           ← every dialog goes through this
    error/StartupErrorScreen.kt
    home/HomeScreen.kt                 ← main weather screen
    navigation/{Screen,AppNavHost}.kt
    theme/Theme.kt
deployment/
  android-upload-signing.json  ← copied from Ruby Slippers — must be replaced before release
  deploy-play-store.ts         ← Play Store upload script
  notify-deploy.ts             ← Discord deploy notification
.github/workflows/build.yml    ← GitHub CI: compile / lint / test / debug-apk
.gitlab-ci.yml                 ← GitLab CI: validate + signed-AAB release pipeline
```

## Data sources

- **Forecast** — `https://api.open-meteo.com/` (public, no auth). Current conditions,
  hourly and daily forecasts, and multi-point grids for the generated forecast
  precipitation animation. The free tier is **non-commercial only** (~10 000 calls/day
  per IP, each location in a multi-point request counts as a call) — a commercial
  release needs Open-Meteo's paid plan.
- **Past radar** — RainViewer tile API for the map's observed-precipitation frames.
