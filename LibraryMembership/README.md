# Library Membership System — Android App

A native Android (Kotlin + Jetpack Compose) rebuild of the **Sub Divisional Library Miao**
membership system, reconstructed from everything discovered across the GitHub repo
(`libraryofmiao/library-membership-system`), the deployed Cloudflare Worker, the Cloudflare
Pages Function (`functions/api/login.js`), and the live pages at `library-membership-system.pages.dev`.

Nothing from the web app's flows was dropped — every screen, field, and action below maps
1:1 to something confirmed live during the review, with the source noted.

## Screens (1:1 with the live site)

| App screen | Web equivalent | Notes |
|---|---|---|
| `LoginScreen` | `login.html` | 6-digit PIN → `POST /api/login` (Pages Function). Same single shared `SECRET_PIN` check confirmed in `functions/api/login.js`. |
| `HomeScreen` | `index.html` | Welcome text, feature cards, Register/Members CTAs, Logout — copied verbatim from the fetched homepage. |
| `DashboardScreen` | `dashboard.html` | Total count, search box, Export/Print/Monthly Report actions, member list with Card/Edit/Delete/Activate-Deactivate — matches the table columns and button labels exactly (`Photo, Member ID, Name, Mobile, Email, Membership, Card, Action, Status`). |
| `RegisterScreen` (also handles edit) | `register.html` / `edit-member.html` | Every field from the live form: Full Name, Guardian, Gender, DOB, Occupation, Address, District, State, PIN Code, Mobile, Email, Membership Type, Membership Duration, ID Proof Type, ID Number, photo upload, declaration checkbox. Includes the **Manage Options** admin panel (`POST /api/options`, `X-Option-Admin-Key` header) exactly as found in `worker.js`. |
| `MemberCardScreen` | `member-card.html` | Photo, Name, Member ID, Email, Membership, Issue Date, Status, QR ("Scan Verification"), Authorized Signatory line, Download PDF / Print / Verify Member actions. |
| `VerifyScreen` | `verify.html` | Calls `GET /api/verify?verify=CODE`, shows only the minimal public fields (name, member ID, membership type, status, photo) — deliberately **not** the full record, per the security recommendation from the review. |

## Backend wiring

- **Worker API** (`MembershipApi.kt`) — points at
  `https://library-membership-system.libraryofmiao.workers.dev/api`, with every route read
  directly from the deployed `worker.js`: `register`, `members`, `member`, `member-status`,
  `verify`, `photo`, `options`.
- **Login** (`AuthApi.kt`) — points at the **Pages** domain
  (`https://library-membership-system.pages.dev`), calling `/api/login`, which is a separate
  Cloudflare Pages Function (`functions/api/login.js`) from the standalone Worker — this
  split is intentional and mirrors the real deployment, not a simplification.
- **NocoDB / GitHub tokens** stay server-side only (inside the Worker/Functions), exactly as
  in the web app — the app never talks to NocoDB or GitHub directly.

## What the app adds beyond the web version

1. **QR camera scanning on the Verify screen** — the web `verify.html` only accepted a code
   already embedded in the URL. The app adds a real camera-based QR scan (ZXing), so a
   phone can scan a printed or on-screen card directly, in addition to manual code entry.
2. **Encrypted local session storage** — replaces the web app's plain `sessionStorage` flag
   with Android's `EncryptedSharedPreferences` (AES-256). This is a hardening improvement,
   but see the security note below: it does **not** fix the underlying gap, because that gap
   lives server-side.
3. **Native PDF export and system Print dialog** for the membership card, instead of the
   web version's browser print/PDF (`androidx.print` + `PdfDocument`).
4. **Client-side search filter** on the dashboard, matching the web app's instant search
   box behavior (name / member ID / mobile / email).

## Security notes carried over from the review (unchanged risk — read before shipping)

The live backend has no server-verified session token:

- `login.js` only returns `{success:true/false}` — it does not issue a token.
- The Worker's member-data routes (`/api/members`, `/api/member`, `PATCH`, `DELETE`) have
  no auth check at all.
- This app's `SessionManager` is deliberately built the same shape as the web app's flag
  (see the doc comment in `SessionManager.kt`) so nothing about the real access-control
  posture is silently "fixed" or hidden by the rebuild — the app is exactly as protected,
  and exactly as exposed, as the website is today.

Recommended fix (not yet implemented, by design — needs your decision on approach): have
`functions/api/login.js` mint a signed, short-lived token on success, store it (e.g., in a
`HttpOnly` cookie or returned to the app to send as `Authorization: Bearer …`), and add a
check for that token to every member-data route in `worker.js`. The app's `ApiClient` and
`MembershipApi` interface are structured so adding an auth header/interceptor later is a
small, localized change — happy to draft that Worker + Functions patch and wire the app up
to it as a follow-up.

## Project structure

```
LibraryMembership/
LibraryMembershipApp/
├── app/
│   ├── build.gradle.kts          # Worker + Pages base URLs as BuildConfig fields
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/libraryofmiao/membership/
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── model/Member.kt          # exact field set from worker.js
│       │   │   ├── network/MembershipApi.kt # every confirmed Worker route
│       │   │   ├── network/ApiClient.kt
│       │   │   └── session/SessionManager.kt
│       │   ├── nav/                          # routes + NavHost
│       │   └── ui/
│       │       ├── login/  home/  dashboard/  register/  membercard/  verify/
│       │       └── theme/                    # colors pulled from login.html/dashboard.html CSS
│       └── res/values/{strings,colors,themes}.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Building

1. Open the `LibraryMembership/` folder in Android Studio (Koala or newer).
2. Let Gradle sync (it will fetch Compose BOM, Retrofit, Coil, ZXing, etc. — no manual setup needed).
3. Run on a device/emulator with API 24+.

No API keys are hardcoded except the public base URLs — the Options-Admin-Key is entered
by the admin at runtime in the "Manage Options" sheet and stored encrypted locally, matching
how the web app expects it to be supplied per-session rather than baked into the client.

## Suggested next steps (optional — say the word and I'll build these too)

- Wire up the real auth-token fix described above (Worker + Pages Function + app interceptor).
- Add pull-to-refresh on the dashboard.
- Add a Room-backed offline cache for the member list.
- Push notifications for membership expiry reminders (would need a new Worker cron + FCM wiring).
- Biometric unlock (fingerprint/face) layered on top of the PIN for a faster admin re-entry.
