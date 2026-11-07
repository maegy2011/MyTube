package com.mohamedadel.mytube

import android.app.PictureInPictureParams
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Rational
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.ui.search.SearchViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    var player: ExoPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        setContent { MyTubeApp(onEnterPip = { enterPip() }, playerProvider = { player }) }
    }
    private fun enterPip() {
        val params = PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
        enterPictureInPictureMode(params)
    }
    override fun onUserLeaveHint() {
        val h = try { player?.videoSize?.height ?: 0 } catch (_: Exception) { 0 }
        if (player?.isPlaying == true && h > 0) enterPip()
        super.onUserLeaveHint()
    }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTubeApp(onEnterPip: () -> Unit, playerProvider: () -> ExoPlayer?) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<VideoItem>()) }
    var playing by remember { mutableStateOf<VideoItem?>(null) }
    var qualityList by remember { mutableStateOf(listOf("audio-only (default)")) }
    var quality by remember { mutableStateOf("audio-only (default)") }
    val scope = rememberCoroutineScope()
    val svm: SearchViewModel = viewModel()
    val suggestions by svm.suggestions.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("MyTube") }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(value = query, onValueChange = { query = it; svm.onQueryChange(it) }, label = { Text("Search") }, modifier = Modifier.fillMaxWidth().padding(8.dp), singleLine = true)
            suggestions.forEach { s ->
                Text(s, Modifier.fillMaxWidth().clickable { query = s; svm.onQueryChange(s); scope.launch { results = YouTubeRepo.search(s).first } }.padding(12.dp, 8.dp))
            }
            Button(onClick = { scope.launch { results = YouTubeRepo.search(query).first } }, modifier = Modifier.padding(8.dp)) { Text("Search") }
            if (playing != null) {
                PlayerArea(playing!!, playerProvider(), quality, qualityList, onQuality = { quality = it }, onFallbackYoutube = { openInYoutube(playing!!) })
                Button(onClick = onEnterPip, modifier = Modifier.padding(8.dp)) { Text("PiP") }
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(results) { v ->
                    Row(Modifier.fillMaxWidth().clickable { playing = v; scope.launch { loadStream(v, playerProvider(), onQualities = { qualityList = it }) } }.padding(8.dp)) {
                        AsyncImage(model = v.thumbnail, contentDescription = null, modifier = Modifier.size(120.dp, 68.dp), contentScale = ContentScale.Crop)
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(v.title ?: "")
                            Text(v.channel ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerArea(video: VideoItem, player: ExoPlayer?, quality: String, qualities: List<String>, onQuality: (String) -> Unit, onFallbackYoutube: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        if (quality != "audio-only (default)" && player != null) {
            AndroidView(factory = { c -> PlayerView(c).apply { this.player = player; layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600) } }, modifier = Modifier.fillMaxWidth().height(200.dp))
        } else {
            AsyncImage(model = video.thumbnail, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
        }
        Text(video.title ?: "")
        Row {
            Box {
                Button(onClick = { expanded = true }) { Text(quality) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    qualities.forEach { q -> DropdownMenuItem(text = { Text(q) }, onClick = { onQuality(q); expanded = false }) }
                }
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = onFallbackYoutube) { Text("Open in YouTube") }
        }
    }
}

suspend fun loadStream(v: VideoItem, player: ExoPlayer?, onQualities: (List<String>) -> Unit) {
    val url = v.url ?: return
    runCatching {
        val s = YouTubeRepo.stream(url)
        val quals = mutableListOf("audio-only (default)", "auto")
        quals.addAll(s.videoUrlByQuality.keys)
        onQualities(quals)
        val audioUrl = s.audioLowestUrl ?: return
        player?.setMediaItem(MediaItem.fromUri(Uri.parse(audioUrl)))
        player?.prepare()
        player?.play()
    }
}

fun android.content.Context.openInYoutube(v: VideoItem) {
    val id = v.id ?: return
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id")).apply { `package` = "com.google.android.youtube" }) }
        .onFailure { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$id"))) }
}
