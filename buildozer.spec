[app]

title = Macro Tracker
package.name = macrotracker
package.domain = org.macrotracker

source.dir = .
source.include_exts = py,png,jpg,kv,atlas,db

version = 1.0

# python3, kivy, kivymd (MD3-capable), sqlite3 is stdlib but pinned here
# explicitly as requested for clarity in the build environment.
requirements = python3,kivy==2.3.0,kivymd==1.2.0,sqlite3,pillow

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
# NDK 25b ships newer GLES headers with a different glVertexAttribPointer
# signature than Kivy 2.3.0's generated C code expects, which breaks the
# build with "too few arguments to function call" errors. NDK 23b is the
# version known to work with Kivy 2.3.0.
android.ndk = 23b
android.accept_sdk_license = True
android.archs = arm64-v8a, armeabi-v7a

[buildozer]

log_level = 2
warn_on_root = 1
