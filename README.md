Oren

Version 1.0

Oren is an Android utility application designed to provide quick and convenient display-orientation controls.

Reverse Portrait

Some devices or situations may make normal portrait orientation unavailable while charging, connecting an OTG device, or during other activities.

Oren provides a Reverse Portrait option, allowing you to switch to reverse portrait orientation when needed.

Quick Access

- Quick Settings Tile — Quickly access Oren's orientation controls.
- Floating Pop-up — Access controls from anywhere on the screen.
- Auto Snap — The floating control automatically snaps to the screen edge.
- Auto Collapse — The floating control can automatically collapse when not in use.
- Animations — Smooth UI animations for a better experience.
- Colorful UI/UX — Designed with a colorful and simple interface.
- Long Press to Close — Long press the floating icon for 5 seconds to close the pop-up.

Permissions

Oren requires:

- Modify system settings — Used to control display orientation.
- Display over other apps — Used for the floating pop-up.
- Background/foreground service — Required to keep the orientation service and floating controls running.

Privacy

No Internet Required

Oren does not require an internet connection.

No Data Collection

Oren does not collect or send your personal data or log information.

The Logger is provided for your use only. Your logs are not collected by us.

Open Source

Oren is open-source software.

The source code is available on GitHub:

GitHub:
https://github.com/Kaviputhalvan/Oren

Source Code

https://drive.google.com/file/d/1gzGzJUdOiNQ8IsBF7qu6L43aCwWSTVkO/view?usp=drivesdk

APK

https://drive.google.com/file/d/1FSsocTicrK2Nx57YtfM1o1Zmj-wnPjIZ/view?usp=drivesdk

Tested Device

- POCO X7 5G

Package

"apk.oren"

Developer

Kaviputhalvan

© 2026 Oren

---

Oren Versions

Version 1.0

Oren-V1.apk

- Type: Unsigned APK
- Initial build of Oren.
- Not signed for installation/distribution.

Oren-V1-signed.apk

- Type: First signed APK
- First signed release of Oren V1.
- Used as the signing baseline for subsequent builds.

---

Patch 1 — P1

Oren-V1-P1.apk

- Patch: P1
- Change: Dummy patch
- No functional change.
- Used as a patch/version testing build.

---

Patch 2 — P2

Oren-V1-P2.apk

- Patch: P2
- Change: Added Close-by Tile Service.
- Added the ability to close/stop Oren through the Quick Settings Tile Service.

---

Patch 3 — P3

Oren-V1-P3.apk

- Patch: P3
- Change: Added System Settings Service breaker.
- Added a mechanism to stop/break the System Settings Service when required.

---

Patch 4 — P4

Oren-V1-P4.apk

- Patch: P4
- Change: Added service breaker when closing the popup.
- Closing the Oren popup now also handles stopping/breaking the related service.

---

Patch 5 — P5

Oren-V1-P5.apk

- Patch: P5
- Change: UI/UX stability improvements.
- Improved popup behavior and overall UI/UX stability.
- Focused on making the interface and interactions more reliable.

---

Patch 5.1 — RP/RL Position Testing

Oren-V1-P5.1.apk — Test 1

- Patch: P5.1
- Test: RP/RL position change — Test 1.
- Tested changing the position/order of the Reverse Portrait (RP) and Reverse Landscape (RL) controls.

Oren-V1-P5.2.apk — Test 2

- Patch: P5.2
- Test: RP/RL position change — Test 2.
- Continued testing the RP/RL control position and ordering.

Oren-V1-P5.3.apk — Test 3

- Patch: P5.3
- Test: RP/RL position change — Test 3.
- Final stability test for the RP/RL position change.
- Result: Stability set/finalized.

«Note: All three P5.1 test builds used the same filename. For historical tracking, they are identified here as Test 1, Test 2, and Test 3.»

---

Patch 6 — P6

Oren-V1-P6.apk

- Patch: P6
- Change: Foreground Service type changed.
- Changed "FloatingBubbleService" from "dataSync" to "specialUse".
- Reason: Android foreground-service restrictions and the "dataSync" time limit.
- The floating bubble service is not a data synchronization service, so "specialUse" is used for its persistent floating orientation-control function.

---

Version History

Build| Type| Main Change
"Oren-V1.apk"| Initial| Unsigned APK
"Oren-V1-signed.apk"| Signed| First signed APK
"Oren-V1-P1.apk"| P1| Dummy patch
"Oren-V1-P2.apk"| P2| Close-by Tile Service
"Oren-V1-P3.apk"| P3| System Settings Service breaker
"Oren-V1-P4.apk"| P4| Service breaker on popup close
"Oren-V1-P5.apk"| P5| UI/UX stability
"Oren-V1-P5.1.apk"| P5.1 Test 1| RP/RL position change
"Oren-V1-P5.2.apk"| P5.2 Test 2| RP/RL position change
"Oren-V1-P5.3.apk"| P5.3 Test 3| RP/RL stability finalized
"Oren-V1-P6.apk"| P6| "dataSync" → "specialUse"

Current Patch

Oren V1 — P6

Latest change: "FloatingBubbleService" foreground service type changed from "dataSync" to "specialUse" to avoid the Android foreground-service "dataSync" time restriction.

---

Releases

Oren has two published releases:

- V1-pre
- V1-P6

The latest release is V1-P6.

---

License

Oren is licensed under the GNU General Public License v3.0 (GPL-3.0).

You are free to use, study, modify, and redistribute Oren under the terms of the GPL-3.0 license.

The source code is provided so that students, developers, and other users can study and learn from the project.

See the ""LICENSE"" (LICENSE) file for the complete license terms.

---

Oren
Version 1.0
Developer: Kaviputhalvan
Package: "apk.oren"

© 2026 Oren
