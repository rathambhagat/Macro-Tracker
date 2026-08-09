[app]

title = Macro Tracker
package.name = macrotracker
package.domain = org.macrotracker

source.dir = .
source.include_exts = py,png,jpg,kv,atlas,db

version = 1.0

# python3, kivy, kivymd (MD3-capable), sqlite3 is stdlib but pinned here
# explicitly as requested for clarity in the build environment.
# Kivy 2.3.1 (not 2.3.0) is required: 2.3.0's GL binding code was written
# against older GLES headers and fails to compile against NDK 25's headers
# ("too few arguments to function call"). 2.3.1 fixed that.
# cython is pinned here (not just in the CI step) because p4a builds Kivy
# via an isolated pip wheel build that resolves its OWN Cython version from
# Kivy's package metadata, ignoring any cython pinned in the outer shell.
# An unpinned/too-new Cython there regenerates the .pyx->.c GL bindings
# with a mismatched function signature, causing the same compile errors.
# python3 is pinned to 3.11.9 because, left unpinned, p4a builds against
# whatever the newest "python3" recipe is (it picked 3.14 and that broke
# pip's own internals plus KivyMD's dependency resolution). kivymd is
# pinned to 1.1.1, the last release verified against this exact chain.
# hostpython3 MUST be pinned to the exact same version as python3 - p4a
# builds them as two separate recipes and hard-fails if they don't match
# ("python3 should have same version as hostpython3").
requirements = python3==3.11.9,hostpython3==3.11.9,cython==0.29.36,kivy==2.3.1,kivymd==1.1.1,sqlite3,pillow

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
# python-for-android now requires NDK >= 25, so we can't downgrade below
# that. Paired with kivy==2.3.1 above, this combination is known to work.
android.ndk = 25b
android.accept_sdk_license = True
android.archs = arm64-v8a, armeabi-v7a

[buildozer]

log_level = 2
warn_on_root = 1
