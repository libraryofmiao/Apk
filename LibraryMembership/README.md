# Library Membership — Android App

A Kotlin + Jetpack Compose Android app that talks directly to your
`library-membership-system` Cloudflare Worker. It covers three roles in one app:

- **Admin** — list/search all members, register new members, edit any field,
  toggle Active/Inactive, delete a member.
- **Staff (Verify)** — live camera QR scanning or manual code entry, calls
  `/api/verify` and shows a clear ACTIVE / INACTIVE result with photo.
- **Member (My Card)** — a member looks themselves up by Member ID and sees
  their card with photo and a QR code (generated locally from their `verify`
  code) to show staff.

## 1. Backend URL — already set

`app/src/main/java/com/library/membership/api/ApiClient.kt` is already
pointed at:

```
https://library-membership-system.libraryofmiao.workers.dev/
```

No edits needed. If you ever redeploy the Worker to a different domain,
update `BASE_URL` in that file (or call `ApiClient.configure("https://...")`
at startup instead).

## 2. Building an APK without a computer

A GitHub Actions workflow is included at
`.github/workflows/android-debug.yml`. It builds a debug APK on every push and
attaches it as a downloadable artifact — no Android Studio or laptop needed:

1. Create a new GitHub repo (can be done from the GitHub mobile app or
   website) and upload/push this whole `LibraryMembership/` folder to it.
2. GitHub will automatically run the "Build debug APK" workflow (or trigger
   it manually from the Actions tab → "Run workflow").
3. When it finishes (a few minutes), open the workflow run → **Artifacts** →
   download `library-membership-debug-apk` — that's a zip containing
   `app-debug.apk`.
4. On your phone, open the downloaded APK to install it (you'll need to
   allow "install unknown apps" for your browser/files app the first time).

This is a **debug** build (unsigned, fine for personal/staff use, not for
the Play Store).

## 3. Building the normal way (if you do have a computer)

1. Open this folder (`LibraryMembership/`) as a project in Android Studio
   (Koala or newer recommended).
2. Let Gradle sync — it will download the Compose, Retrofit, CameraX, ML Kit,
   ZXing, and Coil dependencies listed in `app/build.gradle.kts`.
3. Run on a device or emulator (minSdk 24 / Android 7.0+).

## 3. How it maps to your backend

| App feature | Endpoint used |
|---|---|
| Member list | `GET /api/members` |
| Member detail | `GET /api/member?memberId=` |
| Register | `POST /api/register` |
| Edit member | `PATCH /api/member` |
| Activate/Deactivate | `PATCH /api/member-status` |
| Delete | `DELETE /api/member?memberId=` |
| Staff verify (scan/manual) | `GET /api/verify?verify=` |
| Member card lookup | `GET /api/member-basic?memberId=` |
| Photos | `GET /api/photo?memberId=&verify=` (or `?photoKey=`) |

Note: editing a member on the backend **regenerates their verify code**
(see `handleUpdateMember` in your Worker), which changes their QR code. The
app reloads the member after saving so the new code/photo key are reflected.

## 4. What's not included / known limitations

- **Dropdown option lists are hardcoded to match the current live form**
  (Membership Type: Student, General, Senior Citizen, Research Scholar,
  Faculty · ID Proof Type: Aadhaar Card, Voter ID, PAN Card, Driving
  License, Passport, Govt. ID · Duration: the three options on the form).
  These come from `DEFAULT_MEMBERSHIP_TYPES` / `DEFAULT_ID_TYPES` inside
  `register.html`, managed via the Worker's admin-key-gated
  `/api/options` endpoint (which edits that file on GitHub directly). If
  someone changes those lists on the web, this app's dropdowns will need
  a matching edit in `RegisterScreen.kt` / `MemberDetailScreen.kt` —
  there's no API to fetch them dynamically. The `/api/options` endpoint
  itself isn't wired into the app since it's a rare, admin-key-gated
  config action.
- No offline caching/local database — every screen calls the API live.
- No authentication in front of the Admin/Staff screens — anyone with the
  app can register/edit/delete members and see all data, exactly like the
  current wide-open CORS (`Access-Control-Allow-Origin: *`) setup on the
  Worker. If this app will be used outside a trusted staff group, add an
  admin login layer before shipping it.

## 5. How verification actually works (confirmed from the Worker source)

- On registration, the Worker generates a random 6-character
  alphanumeric `verify` code and stores it in the NocoDB record alongside
  the member.
- The member's QR code (shown on their card) encodes just this 6-character
  code — nothing else.
- Staff scanning that QR calls `GET /api/verify?verify=CODE`, which looks
  the member up by that code and returns their record (including current
  Active/Inactive status).
- **Editing a member's details regenerates their verify code** (and
  therefore their QR code and photo key) — only toggling
  Active/Inactive via `/api/member-status` leaves the code untouched.
  The app reloads the member after any edit so the new code is reflected.

## Project structure

```
app/src/main/java/com/library/membership/
  MainActivity.kt          # NavHost wiring all screens
  api/
    ApiClient.kt            # Retrofit setup + BASE_URL + photo URL helper
    ApiService.kt           # Endpoint definitions
    Models.kt                # Request/response data classes
  ui/
    RoleSelectScreen.kt
    AdminListScreen.kt
    MemberDetailScreen.kt
    RegisterScreen.kt
    VerifyScreen.kt
    MemberCardScreen.kt
    theme/Theme.kt
  util/
    QrUtil.kt                # Generates QR bitmaps (ZXing)
    QrScannerView.kt         # CameraX + ML Kit live scanner
```
