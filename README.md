# Flixr

**CMPE 484 — Mobile Application Development**  
Android project | Kotlin + Jetpack Compose

Flixr is a movie & TV tracking app I built for class. Basically it's like a small social layer on top of TMDB — you can browse films, save a watchlist, mark stuff as watched, write reviews, follow classmates, message people, track TV episodes, and make your own custom lists. All the account stuff and saved data goes through **Firebase** (Auth + Firestore + Storage for profile pics).

Repo: https://github.com/srahzzzz/cmpe484-flixr-mobileapp

---

## What the app does (overview)

I wanted one app where I could:

- find movies without opening five different sites
- remember what I still need to watch
- see what friends rated or reviewed
- track TV shows episode by episode (because I always lose track mid-season)
- chat with people I follow about what we're watching

TMDB gives the posters, descriptions, ratings, genres, TV seasons/episodes. Firebase stores everything that's *mine* — profile, watchlist, reviews, follows, messages, notifications, custom lists, episode checkboxes.

---

## Screens & features

### Before you log in

| Screen | What happens |
|--------|----------------|
| **Splash** | Quick branded splash when the app opens |
| **Welcome** | Login, sign up, or Continue with Google |
| **Login** | Email + password, link to forgot password |
| **Sign up** | Username (must be unique), email, password |
| **Signup success** | Confirmation screen after account is created |
| **Forgot password** | Firebase sends a reset email |
| **Google username** | First-time Google users pick a username once |

Google sign-in needs Firebase configured properly (see setup below).

---

### Bottom navigation (5 tabs)

After login you get five tabs: **Home**, **Discover**, **Mood**, **Activity**, **Me**.

#### Home

- Header says **"Hey &lt;name&gt;"** (or "Hey there") + subtitle
- **Notifications** bell (opens in-app notifications — follows, likes, etc.)
- Shelves: **Trending**, **Popular**, **New releases** (from TMDB)
- **Your TV & episodes** — shows you're tracking with progress (watched count / next episode)
- Tap any movie → **Movie details**

I removed the old "friends activity" block from home because it made the screen too busy — that stuff lives on the **Activity** tab now.

#### Discover

- Search movies by title
- **Filters**: genre, mood, year range, minimum rating
- Active filter chips you can tap to clear
- **Track TV** button in the header (search series on TMDB)
- Results scroll in one list (I refactored this so it doesn't feel like two separate scroll areas)
- Tap a movie → details

#### Mood

- Pick a mood (happy, chill, scary, etc.) and get suggestions
- Sparkle / refresh button shuffles the list
- Same movie detail flow as everywhere else

#### Activity

This tab is the social hub:

- **Feed** of reviews from people you follow
- Like reviews, open the movie if we can match it to TMDB
- **Find users** — search by @username and follow people
- Shortcuts to **Followers**, **Following**, **Messages**

#### Me (Profile)

- **Appearance** — Light / Dark toggle (saved on the device with DataStore). No "system" option in the UI anymore, just pick light or dark.
- **Your library** tiles:
  - **Watchlist** — save for later
  - **Watched** — movies you marked finished
  - **Stats** — charts: counts, avg rating, top genre, runtime estimate, rating histogram
  - **Lists** — custom lists (see below)
- Tabs: **Profile** (edit photo, username, bio) and **My reviews**
- **Sign out**

Followers / Messages / Track TV are reachable from **Activity** or library tiles depending on the flow — the idea was to not duplicate everything on Me.

---

### Screens that stack on top (back button returns you)

| Screen | How to open | Notes |
|--------|-------------|--------|
| **Movie details** | Tap a poster almost anywhere | Watchlist, watched, review (0–10), likes, comments |
| **Watchlist** | Me → Watchlist | Remove items, open movie |
| **Watched** | Me → Watched | History newest first |
| **Stats** | Me → Stats | Analytics from your data + TMDB runtimes |
| **Lists** | Me → Lists | Create / delete lists |
| **List detail** | Tap a list | Rename list, remove movies from list |
| **Notifications** | Home bell | Mark read, tap to navigate when possible |
| **Followers** | Activity | Who follows you |
| **Following** | Activity | Who you follow |
| **Find users** | Activity | Username search |
| **Messages** | Activity | People you follow |
| **Chat** | Messages → person | 1:1 Firestore messages |
| **Other user profile** | From search / feed | See their reviews, follow, message |
| **Track TV** | Discover header or Me flow | Search TV on TMDB |
| **Episode tracking** | Pick a show | Seasons expanded by default, checkboxes, progress bar, **Watch all** / **Clear all** |

---

### Custom lists

You can make lists beyond the normal watchlist:

- Templates when creating: **Watchlist**, **Favorites**, **Ranked**, or **Custom** (auto-fills a name)
- Rename a list on the detail screen
- Remove individual movies from a list

Stored in Firestore collection `UserLists`.

---

### Reviews & social

- Reviews are **per movie**, score **0–10**
- Other users can **like** reviews (Firestore transaction updates count)
- **Comments** on reviews (delete your own)
- **Follow** = you see them in Activity feed + can message them
- **Notifications** for social events (likes, follows, etc.)

Watchlist vs watched: watchlist = "I want to watch this"; watched = "I finished it" from the detail page. They're separate on purpose.

---

## Tech stack

| Layer | What I used |
|-------|-------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Mostly composables + repositories + `AuthViewModel` for auth |
| Backend | Firebase Auth, Firestore, Storage |
| Movies / TV API | [The Movie Database (TMDB)](https://www.themoviedb.org/) via Retrofit |
| Images | Coil |
| Local prefs | DataStore (theme mode) |
| Min SDK | 24 |

Package name: `com.example.flixr` (standard Android Studio default for the course).

---

## Project structure (main folders)

```
app/src/main/java/com/example/flixr/
├── auth/              # Login, signup, profile, AuthViewModel
├── lists/             # UserList model + UserListRepository
├── messages/          # DirectMessage + MessageRepository
├── movies/            # TMDB API, watchlist, watch history, episode tracking
├── notifications/     # AppNotification + NotificationRepository
├── prefs/             # ThemePreferences (light/dark)
├── reviews/           # Reviews, likes, comments
├── social/            # Follow + user discovery
├── stats/             # Analytics helpers
└── ui/                # FlixrApp.kt (most UI), social/list/notification screens, theme
```

`FlixrApp.kt` is huge — that's where most tabs and navigation live. I split some social/list stuff into separate files so it wouldn't be completely impossible to read.

Firestore security rules are in `firestore.rules` at the repo root (collection names match the app: `Reviews`, `Watchlist`, `UserLists`, `Notifications`, etc.).

---

## Setup (if you want to run it yourself)

### 1. Clone the repo

```bash
git clone https://github.com/srahzzzz/cmpe484-flixr-mobileapp.git
cd cmpe484-flixr-mobileapp
```

### 2. Android Studio

- Open the project folder in **Android Studio** (Ladybug or newer should be fine)
- Let Gradle sync — first time takes a while

### 3. TMDB API key

1. Create a free account at https://www.themoviedb.org/
2. Get an API key (v3)
3. In the project root, open or create `local.properties` and add:

```properties
sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk
TMDB_API_KEY=your_actual_key_here
```

**Do not commit your real API key.** `local.properties` is gitignored.

Without a key, movie search and shelves won't load properly.

### 4. Firebase

1. Create a project in [Firebase Console](https://console.firebase.google.com/)
2. Add an **Android app** with package `com.example.flixr`
3. Download `google-services.json` and put it in the `app/` folder
4. Enable **Authentication** (Email/Password and Google if you want Google login)
5. Create **Firestore** database
6. Enable **Storage** if you use profile photo upload
7. Deploy rules from this repo:

```bash
firebase deploy --only firestore:rules
```

(You need Firebase CLI installed and logged in.)

For Google Sign-In you also need the SHA-1 from your debug keystore in Firebase — Android Studio can show this under Gradle signing report, or look up "firebase android sha1" (everyone does this at least once).

### 5. Run on emulator or phone

- Connect a device or start an emulator (API 24+)
- Click **Run** in Android Studio

Or from terminal (Windows):

```powershell
.\gradlew.bat installDebug
```

---

## Building an APK (to install on a phone)

The repo doesn't include a pre-built APK — you build it locally.

**Debug APK** (easiest for testing / submitting a demo file):

```powershell
.\gradlew.bat assembleDebug
```

APK path:

```
app\build\outputs\apk\debug\app-debug.apk
```

Copy that file to your phone and install (allow "unknown apps" if Android asks).

**Release APK** (optional):

```powershell
.\gradlew.bat assembleRelease
```

Output is under `app\build\outputs\apk\release\`. I didn't set up Play Store signing yet — for class, debug is usually enough.

---

## Pushing changes to GitHub

```powershell
git add app/
git commit -m "describe what you changed"
git push origin main
```

(You can `git add .` but I usually skip `.idea` files since those are IDE settings.)

---

## Firestore collections (reference)

Stuff the app reads/writes:

- `users` — profile (username, bio, photo URL)
- `usernames` — unique username lock for signup
- `Watchlist` / `WatchHistory` — saved & watched movies
- `Reviews` — movie reviews
- `Likes` / `ReviewComments` — social on reviews
- `Followers` — follow relationships
- `DirectMessages` — chat
- `Notifications` — in-app alerts
- `UserLists` — custom lists
- `EpisodeTracking` — TV episode checkboxes per user per show

Movie metadata itself is **not** stored in Firestore — it comes from TMDB each time (except IDs you save in lists/history).

---

## Things I ran into / notes

- **Firestore indexes**: Notifications originally used `orderBy` and crashed without a composite index. I fixed it by sorting on the client instead.
- **Theme**: Light/dark is a preference in DataStore; `MainActivity` applies it to `FlixrTheme`. Dynamic Material You colors are off so the pink/navy brand stays consistent.
- **Big file**: `FlixrApp.kt` is still very long — if I had more time I'd move more tabs into separate files.
- **README vs app**: If something here doesn't match the latest build, check the code — I updated the app faster than docs sometimes.

---

## Course info

- **Course**: CMPE 484 — Mobile Application Development  
- **Project name**: Flixr  
- **Platform**: Android (native, Compose)

---

## License / use

This is a course project. Feel free to look at the code for learning, but don't copy-paste the whole thing for another class submission without checking your instructor's academic integrity rules.

If something breaks when you build it, open an issue on GitHub or fix `local.properties` / `google-services.json` first — 90% of the time it's one of those two missing.
