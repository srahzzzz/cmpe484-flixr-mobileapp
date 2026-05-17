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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.flixr.lists.UserList
import com.example.flixr.lists.UserListRepository
import com.example.flixr.movies.Movie
import com.example.flixr.ui.theme.flixrMainSurfaceGradientBrush
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private fun listPosterUrl(path: String?, width: Int): String? {
    val p = path?.trim().orEmpty()
    if (p.isBlank()) return null
    return "https://image.tmdb.org/t/p/w$width$p"
}

@Composable
fun UserListsScreen(
    listRepo: UserListRepository,
    onBack: () -> Unit,
    onOpenList: (UserList) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var lists by remember { mutableStateOf<List<UserList>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            val u = uid ?: return@launch
            loading = true
            try {
                lists = listRepo.getListsForUser(u)
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(uid) { reload() }

    var listTemplate by remember { mutableStateOf("custom") }

    Scaffold(
        topBar = {
            FlixrSubScreenTopBar(
                title = "My lists",
                onBack = onBack,
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(flixrMainSurfaceGradientBrush())
                .padding(padding),
        ) {
            Column(Modifier.fillMaxSize()) {
            when {
                loading ->
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                lists.isEmpty() ->
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Create a list to organize films.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                else ->
                    LazyColumn(
                        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(lists, key = { it.list_id }) { list ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenList(list) },
                            ) {
                                Row(
                                    Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(list.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${list.movie_ids.size} titles",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                listRepo.deleteList(list.list_id)
                                                reload()
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
            }
            Button(
                onClick = { showCreate = true },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Create new list")
            }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New list") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Template", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "watchlist" to "Watchlist",
                            "favorites" to "Favorites",
                            "ranked" to "Ranked",
                            "custom" to "Custom",
                        ).forEach { (id, label) ->
                            androidx.compose.material3.FilterChip(
                                selected = listTemplate == id,
                                onClick = {
                                    listTemplate = id
                                    newName =
                                        when (id) {
                                            "watchlist" -> "Watchlist"
                                            "favorites" -> "Favorites"
                                            "ranked" -> "Ranked"
                                            else -> newName
                                        }
                                },
                                label = { Text(label) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("List name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val u = uid ?: return@Button
                        scope.launch {
                            if (newName.isNotBlank()) {
                                listRepo.createList(u, newName)
                                newName = ""
                                showCreate = false
                                reload()
                            }
                        }
                    },
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
fun UserListDetailScreen(
    list: UserList,
    listRepo: UserListRepository,
    movies: List<Movie>,
    onBack: () -> Unit,
    onListUpdated: (UserList) -> Unit,
    onOpenMovie: (Movie) -> Unit,
) {
    val showRanks = list.name.contains("ranked", ignoreCase = true)
    var showRename by remember { mutableStateOf(false) }
    var renameValue by remember(list.name) { mutableStateOf(list.name) }
    val scope = rememberCoroutineScope()

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text("Rename list") },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("List name") },
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (renameValue.isNotBlank()) {
                                listRepo.renameList(list.list_id, renameValue.trim())
                                onListUpdated(list.copy(name = renameValue.trim()))
                                showRename = false
                            }
                        }
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRename = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            FlixrSubScreenTopBar(
                title = list.name,
                onBack = onBack,
                actions = {
                    TextButton(onClick = { showRename = true }) {
                        Text("Rename")
                    }
                },
            )
        },
    ) { padding ->
        if (movies.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(flixrMainSurfaceGradientBrush())
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No movies in this list yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(flixrMainSurfaceGradientBrush())
                        .padding(padding)
                        .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(movies.size, key = { movies[it].id }) { index ->
                    val movie = movies[index]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOpenMovie(movie) },
                    ) {
                        Box {
                            val url = listPosterUrl(movie.posterPath, width = 185)
                            if (url != null) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = movie.title,
                                    modifier = Modifier.height(160.dp).fillMaxWidth(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Box(Modifier.height(160.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(movie.title, modifier = Modifier.padding(8.dp))
                                }
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        listRepo.removeMovieFromList(list.list_id, movie.id.toString())
                                        onListUpdated(
                                            list.copy(
                                                movie_ids = list.movie_ids.filter { it != movie.id.toString() },
                                            ),
                                        )
                                    }
                                },
                                modifier =
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp),
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Remove from list",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                            if (showRanks) {
                                Surface(
                                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                ) {
                                    Text(
                                        "#${index + 1}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
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

@Composable
fun AddToListDialog(
    listRepo: UserListRepository,
    movieId: String,
    onDismiss: () -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var lists by remember { mutableStateOf<List<UserList>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        val u = uid ?: return@LaunchedEffect
        lists = listRepo.getListsForUser(u)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to list") },
        text = {
            if (lists.isEmpty()) {
                Text("Create a list from Me → Lists first.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(lists, key = { it.list_id }) { list ->
                        TextButton(
                            onClick = {
                                scope.launch {
                                    listRepo.addMovieToList(list.list_id, movieId)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(list.name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}
