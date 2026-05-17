package com.example.flixr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.flixr.messages.DirectMessage
import com.example.flixr.messages.MessageRepository
import com.example.flixr.reviews.ReviewComment
import com.example.flixr.reviews.ReviewCommentRepository
import com.example.flixr.social.FollowRepository
import com.example.flixr.social.UserDiscoveryRepository
import com.example.flixr.ui.theme.FlixrAccent
import com.example.flixr.ui.theme.flixrMainSurfaceGradientBrush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FollowersScreen(
    followRepo: FollowRepository,
    discoveryRepo: UserDiscoveryRepository,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onMessage: (String) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var followerIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var labels by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var err by remember { mutableStateOf<String?>(null) }
    var followingSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        val u = uid
        if (u.isNullOrBlank()) {
            loading = false
            err = "Sign in to see followers."
            return@LaunchedEffect
        }
        loading = true
        err = null
        try {
            followerIds = followRepo.getFollowerIds(u)
            labels = discoveryRepo.loadUsernameLabels(followerIds)
            followingSet = followRepo.getFollowingIds(u).toSet()
        } catch (e: Exception) {
            err = e.message ?: "Could not load followers."
        } finally {
            loading = false
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Back")
        }
        Text("Followers", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "People who follow you. Tap for profile.",
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
            followerIds.isEmpty() ->
                Text(
                    "No followers yet. Share your @username so classmates can find you under Find users.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            else ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(followerIds, key = { it }) { id ->
                        val alreadyFollowing = followingSet.contains(id)
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
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (!alreadyFollowing) {
                                        TextButton(
                                            onClick = {
                                                val u = uid ?: return@TextButton
                                                scope.launch {
                                                    try {
                                                        followRepo.follow(u, id)
                                                        followingSet = followingSet + id
                                                    } catch (e: Exception) {
                                                        err = e.message
                                                    }
                                                }
                                            },
                                        ) {
                                            Text("Follow back")
                                        }
                                    }
                                    TextButton(
                                        onClick = { onMessage(id) },
                                        enabled = alreadyFollowing,
                                    ) {
                                        Text(if (alreadyFollowing) "Message" else "Follow first")
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
fun MessagesHomeScreen(
    followRepo: FollowRepository,
    discoveryRepo: UserDiscoveryRepository,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var following by remember { mutableStateOf<List<String>>(emptyList()) }
    var labels by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var err by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(uid) {
        val u = uid
        if (u.isNullOrBlank()) {
            loading = false
            err = "Sign in to use messages."
            return@LaunchedEffect
        }
        loading = true
        err = null
        try {
            following = followRepo.getFollowingIds(u)
            labels = discoveryRepo.loadUsernameLabels(following)
        } catch (e: Exception) {
            err = e.message ?: "Could not load following list."
        } finally {
            loading = false
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Back")
        }
        Text("Messages", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Chat with people you follow. Pick a name to open the thread.",
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
            following.isEmpty() ->
                Text(
                    "You are not following anyone yet. Follow people from Activity, then return here to message them.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            else ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(following, key = { it }) { peerId ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    labels[peerId] ?: "@${peerId.take(8)}",
                                    fontWeight = FontWeight.SemiBold,
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .clickable { onOpenProfile(peerId) },
                                )
                                TextButton(onClick = { onOpenChat(peerId) }) {
                                    Text("Chat")
                                }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
fun ChatScreen(
    myUid: String,
    peerUid: String,
    messageRepo: MessageRepository,
    onBack: () -> Unit,
) {
    val room = remember(myUid, peerUid) { MessageRepository.chatRoomId(myUid, peerUid) }
    var messages by remember { mutableStateOf<List<DirectMessage>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var peerLabel by remember { mutableStateOf("@${peerUid.take(8)}") }
    val db = remember { FirebaseFirestore.getInstance() }

    LaunchedEffect(peerUid) {
        val snap = db.collection("users").document(peerUid).get().await()
        val un = snap.getString("username").orEmpty()
        if (un.isNotBlank()) peerLabel = "@$un"
    }

    LaunchedEffect(room) {
        try {
            messageRepo.listenMessages(room).collect { messages = it }
        } catch (e: Exception) {
            err = e.message
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Back")
        }
        Text(peerLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(messages, key = { it.message_id }) { m ->
                val mine = m.sender_id == myUid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color =
                            if (mine) {
                                FlixrAccent.copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            },
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            if (!mine) {
                                Text(
                                    m.sender_username.ifBlank { "User" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Text(m.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        if (!err.isNullOrBlank()) {
            Text(err!!, color = MaterialTheme.colorScheme.error)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message…") },
                maxLines = 3,
            )
            IconButton(
                onClick = {
                    if (draft.isBlank()) return@IconButton
                    scope.launch {
                        sending = true
                        err = null
                        try {
                            messageRepo.sendMessage(myUid, peerUid, draft)
                            draft = ""
                        } catch (e: Exception) {
                            err = e.message ?: "Send failed."
                        } finally {
                            sending = false
                        }
                    }
                },
                enabled = !sending && draft.isNotBlank(),
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ReviewCommentsSection(
    reviewId: String,
    commentRepo: ReviewCommentRepository,
    currentUid: String?,
) {
    if (reviewId.isBlank()) return
    var comments by remember(reviewId) { mutableStateOf<List<ReviewComment>>(emptyList()) }
    var draft by remember(reviewId) { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var localErr by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reviewId) {
        try {
            commentRepo.listenCommentsForReview(reviewId).collect { comments = it }
        } catch (_: Exception) {
            comments = emptyList()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Comments", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (comments.isEmpty()) {
            Text("No comments yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (c in comments) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    c.author_username.ifBlank { c.user_id.take(8) },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(c.text, style = MaterialTheme.typography.bodySmall)
                            }
                            if (currentUid != null && c.user_id == currentUid) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                commentRepo.deleteComment(c.comment_id, currentUid)
                                            } catch (e: Exception) {
                                                localErr = e.message
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!localErr.isNullOrBlank()) {
            Text(localErr!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
        if (!currentUid.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment…") },
                    maxLines = 2,
                )
                Button(
                    onClick = {
                        val t = draft.trim()
                        if (t.isBlank()) return@Button
                        scope.launch {
                            busy = true
                            localErr = null
                            try {
                                commentRepo.addComment(reviewId, currentUid, t)
                                draft = ""
                            } catch (e: Exception) {
                                localErr = e.message ?: "Could not post."
                            } finally {
                                busy = false
                            }
                        }
                    },
                    enabled = !busy && draft.isNotBlank(),
                ) {
                    Text("Post")
                }
            }
        } else {
            Text("Sign in to comment.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
