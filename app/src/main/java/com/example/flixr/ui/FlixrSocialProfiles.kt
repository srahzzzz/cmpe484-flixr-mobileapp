package com.example.flixr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.flixr.BuildConfig
import com.example.flixr.auth.UserProfile
import com.example.flixr.movies.TmdbClient
import com.example.flixr.auth.Username
import com.example.flixr.reviews.Review
import com.example.flixr.reviews.ReviewRepository
import com.example.flixr.social.FollowRepository
import com.example.flixr.social.UserDiscoveryRepository
import com.example.flixr.ui.theme.FlixrAccent
import com.example.flixr.ui.theme.flixrMainSurfaceGradientBrush
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    profileUid: String,
    followRepo: FollowRepository,
    reviewRepo: ReviewRepository,
    discoveryRepo: UserDiscoveryRepository,
    onBack: () -> Unit,
    onOpenMovie: (String) -> Unit,
    onMessage: (String) -> Unit,
) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid
    val isSelf = myUid != null && myUid == profileUid
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var followerCount by remember { mutableIntStateOf(0) }
    var followingCount by remember { mutableIntStateOf(0) }
    var isFollowing by remember { mutableStateOf(false) }
    var followsYou by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    var profileTab by remember { mutableIntStateOf(0) }
    var reviewMovieMeta by remember { mutableStateOf<Map<String, Pair<String, String?>>>(emptyMap()) }
    val scope = rememberCoroutineScope()
    val apiKey = BuildConfig.TMDB_API_KEY.trim()

    fun reloadCountsAndFollowState() {
        scope.launch {
            if (myUid.isNullOrBlank()) return@launch
            try {
                followerCount = followRepo.countFollowers(profileUid)
                followingCount = followRepo.getFollowingIds(profileUid).size
                if (!isSelf) {
                    isFollowing = followRepo.isFollowing(myUid, profileUid)
                    followsYou = followRepo.isFollowing(profileUid, myUid)
                }
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(profileUid) {
        loading = true
        err = null
        try {
            profile = discoveryRepo.getUserProfile(profileUid)
            reviews = reviewRepo.getReviewsForUser(profileUid)
            followerCount = followRepo.countFollowers(profileUid)
            followingCount = followRepo.getFollowingIds(profileUid).size
            if (!myUid.isNullOrBlank() && myUid != profileUid) {
                isFollowing = followRepo.isFollowing(myUid, profileUid)
                followsYou = followRepo.isFollowing(profileUid, myUid)
            }
        } catch (e: Exception) {
            err = e.message ?: "Could not load profile."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(reviews, apiKey) {
        if (apiKey.isBlank() || reviews.isEmpty()) {
            reviewMovieMeta = emptyMap()
            return@LaunchedEffect
        }
        val meta = mutableMapOf<String, Pair<String, String?>>()
        for (r in reviews) {
            val id = r.content_id.toIntOrNull() ?: continue
            if (meta.containsKey(r.content_id)) continue
            runCatching {
                val m = TmdbClient.api.getMovieDetails(id, apiKey)
                meta[r.content_id] = (m.title ?: "Movie $id") to m.posterPath
            }.getOrElse {
                meta[r.content_id] = "Movie $id" to null
            }
        }
        reviewMovieMeta = meta
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { FlixrSubScreenTopBar(title = "Profile", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(flixrMainSurfaceGradientBrush())
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
        when {
            loading ->
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            err != null -> Text(err!!, color = MaterialTheme.colorScheme.error)
            else -> {
                val p = profile
                val displayName = p?.username?.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "@${profileUid.take(8)}"
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Surface(shape = CircleShape, modifier = Modifier.size(72.dp), tonalElevation = 2.dp) {
                            val photo = p?.profilePictureUrl
                            if (photo.isNullOrBlank()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp))
                                }
                            } else {
                                AsyncImage(
                                    model = photo,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (followsYou && !isSelf) {
                                Text(
                                    "Follows you",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("$followerCount followers", style = MaterialTheme.typography.bodySmall)
                                Text("$followingCount following", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (!p?.bio.isNullOrBlank()) {
                        Text(p!!.bio, style = MaterialTheme.typography.bodyMedium)
                    }

                    if (!isSelf && !myUid.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        busy = true
                                        err = null
                                        try {
                                            if (isFollowing) {
                                                followRepo.unfollow(myUid, profileUid)
                                                isFollowing = false
                                            } else {
                                                followRepo.follow(myUid, profileUid)
                                                isFollowing = true
                                            }
                                            reloadCountsAndFollowState()
                                        } catch (e: Exception) {
                                            val msg = e.message ?: "Could not update follow."
                                            err = msg
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        } finally {
                                            busy = false
                                        }
                                    }
                                },
                                enabled = !busy,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(if (isFollowing) "Unfollow" else "Follow")
                            }
                            OutlinedButton(
                                onClick = { onMessage(profileUid) },
                                enabled = isFollowing,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(6.dp))
                                Text("Message")
                            }
                        }
                        if (!isFollowing) {
                            Text(
                                "Follow to message this person.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = profileTab,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    ) {
                        Tab(selected = profileTab == 0, onClick = { profileTab = 0 }, text = { Text("About") })
                        Tab(
                            selected = profileTab == 1,
                            onClick = { profileTab = 1 },
                            text = { Text("Reviews (${reviews.size})") },
                        )
                    }

                    when (profileTab) {
                        0 ->
                            Text(
                                if (isSelf) {
                                    "This is your public profile. Edit details in Me → Profile."
                                } else {
                                    "Public reviews and activity from people you follow appear on the Activity tab."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        else -> {
                            if (reviews.isEmpty()) {
                                Text("No reviews yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                reviews.forEach { r ->
                                    val (title, poster) = reviewMovieMeta[r.content_id] ?: ("Movie ${r.content_id}" to null)
                                    ReviewListCard(
                                        review = r,
                                        movieTitle = title,
                                        posterPath = poster,
                                        onClick = { onOpenMovie(r.content_id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun FollowingScreen(
    followRepo: FollowRepository,
    discoveryRepo: UserDiscoveryRepository,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onUnfollow: (String) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var followingIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var labels by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var err by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        val u = uid
        if (u.isNullOrBlank()) {
            loading = false
            err = "Sign in to see who you follow."
            return@LaunchedEffect
        }
        loading = true
        err = null
        try {
            followingIds = followRepo.getFollowingIds(u)
            labels = discoveryRepo.loadUsernameLabels(followingIds)
        } catch (e: Exception) {
            err = e.message ?: "Could not load following."
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = { FlixrSubScreenTopBar(title = "Following", onBack = onBack) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
    ) { padding ->
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            "People you follow. Tap a name for their profile.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        when {
            loading ->
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            err != null -> Text(err!!, color = MaterialTheme.colorScheme.error)
            followingIds.isEmpty() ->
                Text(
                    "You are not following anyone yet. Find people from Activity → Find users.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            else ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(followingIds, key = { it }) { id ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenProfile(id) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(labels[id] ?: "@${id.take(8)}", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        id,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        onUnfollow(id)
                                        followingIds = followingIds.filter { it != id }
                                    },
                                ) {
                                    Text("Unfollow")
                                }
                            }
                        }
                    }
                }
        }
    }
    }
}

@Composable
fun UserSearchScreen(
    discoveryRepo: UserDiscoveryRepository,
    followRepo: FollowRepository,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    val myUid = FirebaseAuth.getInstance().currentUser?.uid
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    var followingSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(myUid) {
        val u = myUid ?: return@LaunchedEffect
        try {
            followingSet = followRepo.getFollowingIds(u).toSet()
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(query) {
        val q = query.trim()
        if (q.length < 2) {
            results = emptyList()
            return@LaunchedEffect
        }
        loading = true
        err = null
        try {
            results = discoveryRepo.searchUsernamesByPrefix(q)
        } catch (e: Exception) {
            err = e.message ?: "Search failed."
            results = emptyList()
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = { FlixrSubScreenTopBar(title = "Find users", onBack = onBack) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
    ) { padding ->
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            "Search by username (at least 2 characters).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(12.dp))
        when {
            loading ->
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FlixrAccent)
                }
            err != null -> Text(err!!, color = MaterialTheme.colorScheme.error)
            query.trim().length < 2 ->
                Text("Type to search classmates.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            results.isEmpty() ->
                Text("No users found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(results, key = { it.second }) { (name, uid) ->
                        if (uid == myUid) return@items
                        val already = followingSet.contains(uid)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .clickable { onOpenProfile(uid) },
                                ) {
                                    Text("@$name", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        uid,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                if (already) {
                                    TextButton(onClick = { onOpenProfile(uid) }) {
                                        Text("Profile")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val u = myUid ?: return@Button
                                            scope.launch {
                                                try {
                                                    followRepo.follow(u, uid)
                                                    followingSet = followingSet + uid
                                                } catch (e: Exception) {
                                                    err = e.message
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Text("Follow")
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
    }
}

/** Resolve @label for a UID; falls back to shortened id. */
fun usernameLabel(labels: Map<String, String>, uid: String): String =
    labels[uid] ?: "@${uid.take(8)}"
