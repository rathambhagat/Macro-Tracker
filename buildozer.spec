[app]

title = Macro Tracker
package.name = macrotracker
package.domain = org.macrotracker

source.dir = .
source.include_exts = py,png,jpg,kv,atlas
source.exclude_dirs = .git,.buildozer,.buildozer_global,bin,__pycache__,.github

version = 1.0

requirements = python3==3.11,kivy==2.3.1,kivymd==1.1.1,pillow==9.5.0,sqlite3,filetype

orientation = portrait
fullscreen = 0

android.permissions = INTERNET

android.api = 33
android.sdk = 33
android.minapi = 21
android.build_tools = 33.0.2

android.accept_sdk_license = True
android.skip_update = False

android.enable_androidx = True

android.archs = arm64-v8a

android.ndk = 25b

p4a.branch = v2024.01.21

[buildozer]

log_level = 2
warn_on_root = 0