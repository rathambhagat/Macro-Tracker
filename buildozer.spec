[app]

title = Macro Tracker
package.name = macrotracker
package.domain = org.macrotracker

source.dir = .
source.include_exts = py,png,jpg,kv,atlas,db

version = 1.0

# We build inside the official kivy/buildozer Docker image (see the
# GitHub Actions workflow), which ships its own tested, matched set of
# NDK / SDK / Python / pip. That eliminates the whole class of errors we
# hit trying to hand-assemble that toolchain on a bare Ubuntu runner
# (NDK/GL mismatches, python3-vs-hostpython3 version drift, broken pip
# internals). We still pin the pure-Python/package-level versions below,
# because these conflicts come from the packages themselves, not the host:
#   - kivy==2.3.1: 2.3.0's GL binding code doesn't compile against modern
#     NDK GLES headers ("too few arguments to function call").
#   - pillow==9.5.0: every kivymd release in the 0.102.0-2.0.0 range
#     requires Pillow<10.0.0; an unpinned Pillow grabs 11.x and makes
#     EVERY kivymd version unresolvable ("ResolutionImpossible").
#   - kivymd==1.1.1: verified compatible with the above two pins.
requirements = python3,cython==0.29.36,kivy==2.3.1,kivymd==1.1.1,pillow==9.5.0,sqlite3

orientation = portrait
fullscreen = 0

# Add your own icon later if you want one:
# icon.filename = %(source.dir)s/icon.png

# ---------------------------------------------------------------------------
# Android specifics
# ---------------------------------------------------------------------------
android.permissions = INTERNET
android.api = 33
android.minapi = 21
android.accept_sdk_license = True
android.archs = arm64-v8a, armeabi-v7a

[buildozer]

log_level = 2
warn_on_root = 1
