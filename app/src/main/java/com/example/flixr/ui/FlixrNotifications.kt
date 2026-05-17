package com.example.flixr.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.example.flixr.notifications.AppNotification
import com.example.flixr.notifications.NotificationRepository
import com.example.flixr.ui.theme.flixrMainSurfaceGradientBrush
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    notificationRepo: NotificationRepository,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenMovie: (String) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var items by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        val u = uid
        if (u.isNullOrBlank()) {
            loading = false
            return@LaunchedEffect
        }
        notificationRepo.listenNotifications(u).collect {
            items = it
            loading = false
        }
    }

    Scaffold(
        topBar = {
            FlixrSubScreenTopBar(
                title = "Notifications",
                onBack = onBack,
                actions = {
                    TextButton(
                        onClick = {
                            val u = uid ?: return@TextButton
                            scope.launch { notificationRepo.markAllRead(u) }
                        },
                    ) {
                        Text("Mark all read")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(flixrMainSurfaceGradientBrush())
                    .padding(padding),
        ) {
            when {
                loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                items.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notifications yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                else ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items, key = { it.notification_id }) { n ->
                            val label =
                                when (n.type) {
                                    "follow" -> "@${n.actor_username} started following you"
                                    "unfollow" -> "@${n.actor_username} unfollowed you"
                                    "message" -> "@${n.actor_username} sent you a message"
                                    "like" -> "@${n.actor_username} liked your review"
                                    else -> "Update from @${n.actor_username}"
                                }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color =
                                    if (n.read) {
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch {
                                                notificationRepo.markRead(n.notification_id)
                                                when (n.type) {
                                                    "follow", "unfollow" -> onOpenProfile(n.actor_id)
                                                    "message" -> onOpenChat(n.actor_id)
                                                    "like" ->
                                                        if (n.content_id.isNotBlank()) {
                                                            onOpenMovie(n.content_id)
                                                        } else {
                                                            onOpenProfile(n.actor_id)
                                                        }
                                                    else -> onOpenProfile(n.actor_id)
                                                }
                                            }
                                        },
                            ) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(label, fontWeight = if (n.read) FontWeight.Normal else FontWeight.SemiBold)
                                    if (!n.read) {
                                        Text(
                                            "New",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
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
