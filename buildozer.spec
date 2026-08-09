[app]

title = Macro Tracker
package.name = macrotracker
package.domain = org.macrotracker

source.dir = .
source.include_exts = py,png,jpg,kv,atlas
source.exclude_dirs = .git,.buildozer,.buildozer_global,bin,__pycache__,.github

version = 1.0

# Pin versions that are known to work together.
# Pillow must stay below 10 for KivyMD 1.1.1.
requirements = python3,kivy==2.3.1,kivymd==1.1.1,pillow==9.5.0,sqlite3,cython==0.29.36

orientation = portrait
fullscreen = 0

# ---------------------------------------------------------------------------
# Android specifics
# ---------------------------------------------------------------------------
android.permissions = INTERNET
android.api = 33
android.minapi = 21
android.accept_sdk_license = True
android.skip_update = True
android.enable_androidx = True

# Build only one architecture first.
# Add armeabi-v7a later after you get a successful APK.
android.archs = arm64-v8a

# Use an older NDK that is much safer for Kivy 2.3.x.
android.ndk = 25b

# Pin python-for-android to an older stable tag.
# This prevents Buildozer from pulling the newest incompatible toolchain.
p4a.branch = v2024.01.21

[buildozer]

log_level = 2
warn_on_root = 0