package com.mohamedadel.mytube

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Rational
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    private var player: ExoPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        val p = player
        setContent { MyTubeApp(onEnterPip = { enterPip() }, player = p) }
    }
    private fun enterPip() {
        val params = PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
        enterPictureInPictureMode(params)
    }
    override fun onUserLeaveHint() {
        val h: Int = try { player?.videoSize?.height ?: 0 } catch (_: Exception) { 0 }
        val playing: Boolean = try { player?.isPlaying == true } catch (_: Exception) { false }
        if (playing && h > 0) enterPip()
        super.onUserLeaveHint()
    }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTubeApp(onEnterPip: () -> Unit, player: ExoPlayer?) {
    var query: String by remember { mutableStateOf("") }
    var results: List<VideoItem> by remember { mutableStateOf(emptyList()) }
    var playing: VideoItem? by remember { mutableStateOf(null) }
    var qualityList: List<String> by remember { mutableStateOf(listOf("audio-only (default)")) }
    var quality: String by remember { mutableStateOf("audio-only (default)") }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val svm: SearchViewModel = viewModel()
    val suggestions: List<String> by svm.suggestions.collectAsState()

    fun doSearch(q: String) { scope.launch { results = YouTubeRepo.search(q).first } }
    fun playVideo(v: VideoItem) {
        playing = v
        scope.launch {
            val s = YouTubeRepo.stream(v.url ?: return@launch)
            val quals = mutableListOf("audio-only (default)", "auto")
            quals.addAll(s.videoUrlByQuality.keys)
            qualityList = quals
            val audioUrl = s.audioLowestUrl ?: return@launch
            player?.setMediaItem(MediaItem.fromUri(Uri.parse(audioUrl)))
            player?.prepare()
            player?.play()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("MyTube") }) }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(value = query, onValueChange = { query = it; svm.onQueryChange(it) }, label = { Text("Search") }, modifier = Modifier.fillMaxWidth().padding(8.dp), singleLine = true)
            for (s in suggestions) {
                Text(s, modifier = Modifier.fillMaxWidth().clickable { query = s; svm.onQueryChange(s); doSearch(s) }.padding(12.dp, 8.dp))
            }
            Button(onClick = { doSearch(query) }, modifier = Modifier.padding(8.dp)) { Text("Search") }
            val cur: VideoItem? = playing
            if (cur != null) {
                PlayerArea(cur, player, quality, qualityList, onQuality = { quality = it }, onFallbackYoutube = { ctx.openInYoutubeCtx(cur) })
                Button(onClick = onEnterPip, modifier = Modifier.padding(8.dp)) { Text("PiP") }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = results) { v ->
                    SearchRow(v = v, onClick = { playVideo(v) })
                }
            }
        }
    }
}

@Composable
fun SearchRow(v: VideoItem, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(8.dp)) {
        AsyncImage(model = v.thumbnail, contentDescription = null, modifier = Modifier.size(120.dp, 68.dp), contentScale = ContentScale.Crop)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(v.title ?: "")
            Text(v.channel ?: "", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PlayerArea(video: VideoItem, player: ExoPlayer?, quality: String, qualities: List<String>, onQuality: (String) -> Unit, onFallbackYoutube: () -> Unit) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
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
                    for (q in qualities) {
                        DropdownMenuItem(text = { Text(q) }, onClick = { onQuality(q); expanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onFallbackYoutube) { Text("Open in YouTube") }
        }
    }
}

fun Context.openInYoutubeCtx(v: VideoItem) {
    val id = v.id ?: return
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id")).apply { `package` = "com.google.android.youtube" })
    } catch (_: Exception) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$id")))
    }
}
