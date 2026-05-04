# Flixr

Flixr is an Android app for movie fans. You browse titles from **TMDB**, keep lists of what you want to watch and what you finished, rate and review films, follow friends, see their activity, track TV episodes episode-by-episode, and chat with people you follow. Accounts and saved data live in **Firebase** (sign-in, profiles, Firestore, optional profile photos).

---

## Before you are signed in

- **Splash** — Short branded opening when the app starts.
- **Welcome** — Choose **Login**, **Create account**, or **Continue with Google** (Google needs Firebase setup).
- **Login** — Email and password, link to **Forgot password** (sends a Firebase reset email).
- **Sign up** — Pick a unique username, email, and password; then you can log in.
- **Forgot password** — Enter your email to receive a password reset link.
- **Google username** — If you sign in with Google for the first time, you pick a unique username once before entering the main app.

---

## Main app (bottom navigation)

After sign-in you always have five tabs at the bottom:

### Home

- Personalized header with your name and a search shortcut that jumps to **Discover**.
- **Trending**, **popular**, and **new releases** shelves.
- **Watchlist preview** and a **Friends activity** section (reviews from people you follow).
- Tap a movie to open **Movie details**. Notifications icon is reserved for future use.

### Discover

- **Search** by title plus filters: **genre**, **mood**, **year range**, **minimum rating**.
- Results open **Movie details**.
- A floating **+** button on the main shell also jumps here so you can add or find titles quickly.

### Mood

- Pick how you feel (mood presets) and get a **mood-matched** list of suggestions.
- Tap a title for **Movie details**.
- Refresh control to shuffle suggestions.

### Activity

- **Social feed**: reviews from users you **follow**.
- **Follow** new people by user id, **like** reviews, and open a film from a review when the app can resolve it to a TMDB movie.

### Profile (Me)

- **Tips card** — Short explanation of where watched movies and TV tracking live.
- **Your library** — Tappable tiles:
  - **Watchlist** — Saved “watch later” titles; remove items or open a movie.
  - **Watched** — Films you marked as watched on detail pages (newest first).
  - **Stats** — Analytics: watchlist count, watched count, reviews, average rating, top genre, estimated watch time from TMDB runtimes, rating histogram, genre bars.
  - **Track TV** — Search a TV series, then open **Episode tracking** for that show.
  - **Followers** — List of accounts that follow you (with @username when available).
  - **Messages** — List of people **you follow**; tap one to open a **1:1 chat** (real-time messages in Firestore).
  - **Following & activity** — Jumps to the **Activity** tab.
- **Tabs**: **Profile** (edit avatar, username, bio, save) and **My reviews** (your written reviews).
- **Sign out**.

---

## Screens that open on top of the tabs

These replace the tab shell until you go back (you usually return to the tab you came from):

| Screen | How you get there | What it does |
|--------|---------------------|----------------|
| **Movie details** | Tap almost any movie poster or row | Large poster/hero, overview, TMDB rating, **add to watchlist**, **mark as watched**, **write/update/delete your review** (0–10), list of **reviews** with **likes**, **comments** on each review, delete your own comments. |
| **Watchlist** | Profile → Watchlist | All saved movies; remove or open a title. |
| **Watched** | Profile → Watched (or Stats → open watched list when you have watched items) | All titles you marked watched. |
| **Stats / Analytics** | Profile → Stats | Numbers and charts described under Profile. |
| **Followers** | Profile → Followers | Who follows you. |
| **Messages** | Profile → Messages | People you follow; start a chat. |
| **Chat** | Messages → pick a person | Thread with that user; send text. |
| **TV search** | Profile → Track TV | Search TMDB for a series. |
| **Episode tracking** | Pick a show from TV search | Seasons and episodes with **checkboxes**; progress bar; data stored in Firestore **EpisodeTracking**. |

---

## Data and behavior (short)

- **Watchlist** and **watched** are separate: watchlist is “save for later”; watched is “I finished this” from the detail screen.
- **Reviews** are per movie, 0–10, with **likes** and **comments**.
- **Follow** relationships power the **Activity** feed and who appears under **Messages**.
- **Episode tracking** is per user and per show (season/episode watched flags).

---

## Tech stack

- **Kotlin**, **Jetpack Compose**, **Material 3**
- **Firebase** Auth, Firestore, Storage (profile pictures)
- **TMDB** API for catalogue and images  
- Course project **CMPE 484** — package `com.example.flixr`.
