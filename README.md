# Macro Tracker (Offline, KivyMD)

A minimalist, offline-only Android app for logging raw macros (Kcal, Protein,
Carbs, Fats) — no food names, no ingredient database. Built with KivyMD
(Material Design 3, dark theme) and SQLite3, packaged with Buildozer.

## Project layout

```
.
├── main.py                        # Full app: dashboard, settings, history
├── buildozer.spec                 # Android build configuration
├── .github/workflows/build.yml    # CI: builds the APK on every push to main
└── README.md
```

## Building locally (optional)

You don't need this if you're using GitHub Actions, but for reference:

```bash
pip install buildozer cython==0.29.36
buildozer android debug
```

The first local build downloads the Android SDK/NDK and can take 20–40
minutes. Subsequent builds are much faster.

## Building via GitHub Actions (recommended for your setup)

1. Create a new GitHub repository and push these three items to it:
   `main.py`, `buildozer.spec`, and the `.github/workflows/build.yml` file
   (keep the `.github/workflows/` folder structure exactly as-is).
2. Push to the `main` branch, or open the repo's **Actions** tab and run the
   "Build Android APK" workflow manually (`workflow_dispatch`).
3. The build takes roughly 15–30 minutes on a fresh run (dependencies are
   cached after that, so later builds are faster).
4. When it finishes, open the completed workflow run → **Artifacts** →
   download `macro-tracker-apk`. Unzip it to get the `.apk` file.
5. Transfer the APK to your Android device and install it (you'll need to
   allow "install from unknown sources" for whichever app you use to open
   the file).

## Notes on the code

- **No food names/ingredients anywhere** — the Quick Add card only exposes
  four numeric fields (Kcal, Protein, Carbs, Fats).
- **Blank-field safety**: `safe_float()` in `main.py` converts blank or
  invalid input to `0.0` instead of crashing; logging is blocked only if
  *all four* fields are empty/zero.
- **Midnight reset**: the dashboard always queries `entries` for *today's*
  date only, so progress naturally resets at midnight without deleting any
  history. Past days remain queryable in the History screen.
- **Database location**: SQLite file is stored in the app's private
  `user_data_dir`, so it persists across app restarts and is wiped only if
  the user uninstalls the app or clears app data.
- **Targets**: stored in a single-row `goals` table; editing them on the
  Settings screen immediately changes the progress bar maximums.

## Customizing

- Change default targets in `Database._create_tables()` (the initial
  `INSERT INTO goals ...` line).
- Change the color theme in `MacroTrackerApp.build()`
  (`theme_cls.primary_palette`, `theme_cls.accent_palette`).
- Add an app icon by placing `icon.png` next to `buildozer.spec` and
  uncommenting the `icon.filename` line inside it.
