#!/usr/bin/env python3
"""Rebrand an imported Telegram Android client while retaining GPL notices.

Run only in the GLACIA feature branch after the upstream source has been merged.
This is an initial automated branding pass, NOT a claim of build readiness.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent.parent
REPO = "https://github.com/void-tech-shiv/KLTG"
OLD_JAVA = "uz/unnarsx/cherrygram"
NEW_JAVA = "dev/voidtech/glacia"

# User-facing identity + internal names; preserve legally required copyright headers.
REPLACEMENTS = (
    ("uz.unnarsx.cherrygram", "dev.voidtech.glacia"),
    ("uz/unnarsx/cherrygram", "dev/voidtech/glacia"),
    ("uz_unnarsx_cherrygram", "dev_voidtech_glacia"),
    ("CHERRYGRAM", "GLACIA"), ("Cherrygram", "Glacia"),
    ("cherrygram", "glacia"),
    ("CHERRY", "GLACIA"), ("Cherry", "Glacia"), ("cherry", "glacia"),
)
EXTENSIONS = {
    ".java", ".kt", ".xml", ".gradle", ".properties", ".md", ".txt",
    ".json", ".yml", ".yaml", ".py", ".sh", ".kts", ".pro", ".cfg",
    ".c", ".cc", ".cpp", ".h", ".hpp", ".cmake", ".html", ".css", ".js"
}
EXCLUDED = {".git", ".github", "LICENSE", "COPYING", "ATTRIBUTION.md"}

def transform(text: str) -> str:
    # Preserve source origin and copyright attribution, but stop old promo links.
    text = re.sub(r"https?://t\.me/(?:cherrygram[A-Za-z0-9_]*)", REPO, text, flags=re.I)
    text = re.sub(r"@Cherrygram[A-Za-z0-9_]*", "GLACIA", text, flags=re.I)
    text = re.sub(r"https?://arslan4k1390\.github\.io/cherrygram/[A-Za-z0-9_/?#=&.-]*", REPO, text, flags=re.I)
    text = re.sub(r"https?://crowdin\.com/project/cherrygram", REPO, text, flags=re.I)
    text = re.sub(r"https?://github\.com/arsLan4k1390/Cherrygram", REPO, text, flags=re.I)

    head, body = "", text
    if body.startswith("/**") and "Copyright" in body[:800]:
        end = body.find("*/")
        if end >= 0:
            head, body = body[:end + 2], body[end + 2:]
    for old, new in REPLACEMENTS:
        body = body.replace(old, new)
    return head + body

changed = 0
for p in list(ROOT.rglob("*")):
    if not p.is_file() or p == Path(__file__).resolve() or any(part in EXCLUDED for part in p.relative_to(ROOT).parts):
        continue
    if p.suffix.lower() not in EXTENSIONS and p.name not in {".gitignore", ".gitmodules", "Dockerfile"}:
        continue
    if p.stat().st_size > 8_000_000:
        continue
    try:
        data = p.read_text(encoding="utf-8")
    except (UnicodeError, OSError):
        continue
    updated = transform(data)
    if data != updated:
        p.write_text(updated, encoding="utf-8")
        changed += 1

# Move source-package paths and rebranded resources along with their references.
pkg = ROOT / "TMessagesProj/src/main/java" / OLD_JAVA
if pkg.exists():
    new_pkg = ROOT / "TMessagesProj/src/main/java" / NEW_JAVA
    new_pkg.parent.mkdir(parents=True, exist_ok=True)
    if new_pkg.exists():
        raise RuntimeError("Destination package directory exists: " + str(new_pkg))
    pkg.rename(new_pkg)

for path in sorted(ROOT.rglob("*"), key=lambda x: len(x.parts), reverse=True):
    if ".git" in path.parts or ".github" in path.parts:
        continue
    if not path.exists():
        continue
    new_name = path.name
    for old, new in REPLACEMENTS:
        # Only branding/name components, not a broad rename of numeric or common files.
        if old in new_name and old != "uz.unnarsx.cherrygram" and old != "uz/unnarsx/cherrygram":
            new_name = new_name.replace(old, new)
    if new_name != path.name:
        dest = path.with_name(new_name)
        if dest.exists():
            raise RuntimeError("Refusing to overwrite " + str(dest))
        path.rename(dest)

# Neutralize upstream channels, linked personal accounts, OTA and support destinations.
constants = ROOT / "TMessagesProj/src/main/java/dev/voidtech/glacia/misc/Constants.kt"
if constants.exists():
    s = constants.read_text(encoding="utf-8")
    s = re.sub(r'var CG_AUTHOR = .*', 'var CG_AUTHOR = "GLACIA"', s)
    s = re.sub(r'(var CG_(?:CHANNEL|APKS_CHANNEL|BETA_APKS_CHANNEL|CHAT|OFFTOPIC_CHAT)_USERNAME = )".*?"',
               r'\1""', s)
    s = re.sub(r'(var CG_(?:CHANNEL|APKS_CHANNEL|BETA_APKS_CHANNEL|CHAT)_URL = )".*?"',
               r'\1"' + REPO + '"', s)
    s = re.sub(r'var UPDATE_APP_URL = .*', 'var UPDATE_APP_URL = "' + REPO + '"', s)
    s = re.sub(r'const val Glacia_(?:Owner|Channel|Support|APKs|Beta|Archive) = \d+L',
               lambda m: m.group(0).split(" = ")[0] + " = 0L", s)
    # Never route GLACIA users to a legacy private chat or former owner's ID.
    constants.write_text(s, encoding="utf-8")

# Suppress upstream promotion/community section, leaving the source/GitHub link.
about = ROOT / "TMessagesProj/src/main/java/dev/voidtech/glacia/preferences/AboutPreferencesEntry.java"
if about.exists():
    s = about.read_text(encoding="utf-8")
    start = s.find("items.add(UItem.asHeader(getString(R.string.CGP_Links)));")
    end = s.find("if (!GlaciaCoreConfig.isStandalonePremiumBuild())", start)
    if start != -1 and end != -1:
        s = s[:start] + "items.add(UItem.asHeader(getString(R.string.CGP_Links)));\n        " + s[end:]
    about.write_text(s, encoding="utf-8")

# Remove code paths that can process remote messages to clear the user's app data.
def neutralize_function(path: Path, marker: str):
    if not path.exists():
        return
    s = path.read_text(encoding="utf-8")
    idx = s.find(marker)
    if idx == -1:
        return
    opening = s.find("{", idx)
    if opening == -1:
        return
    level, closing = 0, None
    for i in range(opening, len(s)):
        if s[i] == "{":
            level += 1
        elif s[i] == "}":
            level -= 1
            if level == 0:
                closing = i
                break
    if closing is not None:
        s = s[:opening] + "{ /* GLACIA: remote-triggered account-data removal disabled. */ }" + s[closing + 1:]
        path.write_text(s, encoding="utf-8")

neutralize_function(ROOT / "TMessagesProj/src/main/java/org/telegram/messenger/KotlinFragmentsManager.kt",
                    "suspend fun iooewwfueuewu121(")
neutralize_function(ROOT / "TMessagesProj/src/main/java/org/telegram/messenger/SamsungDatastore.kt",
                    "suspend fun vpwogjigjjur232(")

core = ROOT / "TMessagesProj/src/main/java/dev/voidtech/glacia/core/configs/GlaciaCoreConfig.kt"
if core.exists():
    s = core.read_text(encoding="utf-8")
    s = s.replace("        loadMessages()", "        // Upstream remote message loader disabled in GLACIA.")
    s = re.sub(r'        if \(\s*KotlinFragmentsManager\.vreg42r2r2r1r3q1rq3\(.*?\n        \}\n',
               '        // Upstream name-lock checks disabled for independent branding.\n', s, flags=re.S)
    s = s.replace('var autoOTA by sharedPreferences.boolean("CG_Check_Auto_OTA", true)',
                  'var autoOTA by sharedPreferences.boolean("CG_Check_Auto_OTA", false)')
    core.write_text(s, encoding="utf-8")

readme = """# GLACIA — Telegram-compatible Android client

An independent, community-modified Android Telegram client maintained at
[void-tech-shiv/KLTG](https://github.com/void-tech-shiv/KLTG).

## Development status
This branch is an initial upstream source import and GLACIA branding pass.
**It has NOT passed an Android build or on-device test.** Existing upstream
messaging UI and functionality are preserved in source, but some upstream files
are missing and need to be recreated from your own credentials and implementation.

## Local setup
1. Install Android Studio, Android SDK 36, Android NDK 27.2.12479018 and CMake 3.22.1.
2. Clone recursively: `git clone --recurse-submodules https://github.com/void-tech-shiv/KLTG.git`
   (check out the GLACIA PR branch before merging).
3. Initialize nested submodules: `git submodule update --init --recursive --depth 1`.
4. Supply **your own** Telegram application ID/hash using the project's `Extra.kt`
   integration. Never commit API credentials, signing keys, or Firebase JSON.
5. Configure your own Firebase projects if you need cloud push.
6. Generate your own signing key for distribution.
7. Open the repository root in Android Studio and Gradle Sync.

## Known blockers
- The upstream `Extra.kt` is ignored/not distributed; required by BuildVars.
- Several upstream Java/Kotlin helper files are listed in .gitignore and may be unavailable.
- Native submodules, Firebase JSON, and signing configuration need full verification.
- Review inherited billing/donations, remote update handlers, analytics and security
  functionality before shipping any APK.
- New icon artwork and full brand-resource audit still require verification.
- Not an official Telegram application.

## License and provenance
GPL-2.0; see [LICENSE](LICENSE) and [ATTRIBUTION.md](ATTRIBUTION.md).
All original notices and author copyright headers are retained where applicable.
GLACIA product names and links do not imply original authors' endorsement.
"""
(ROOT / "README.md").write_text(readme, encoding="utf-8")
attribution = """# Third-party source attribution

GLACIA incorporates code derived from the GPL-licensed Cherrygram project:
https://github.com/arsLan4k1390/Cherrygram

That project is itself based on the official Telegram Android project:
https://github.com/DrKLO/Telegram

The original GPL license and copyright headers remain in the distribution;
this notice documents the provenance of the code. This is not an official
Telegram app, and the upstream maintainers do not endorse GLACIA.

Changes starting 2026-10-24: GLACIA application identity, product name,
package namespace, repository links, display strings and some vendor-specific
remote-control code paths were modified. Further independent review is required.
"""
(ROOT / "ATTRIBUTION.md").write_text(attribution, encoding="utf-8")
print(f"Updated {changed} text files; GLACIA source imported and branded (unverified).")
