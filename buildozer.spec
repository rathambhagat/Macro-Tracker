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
requirements = python3,kivy==2.3.1,kivymd==1.2.0,sqlite3,pillow

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
