package com.mohamedadel.mytube.ui.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.data.youtube.CommentItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Phase 2.3 slice 7 (2026-11-13): video comments extracted from the
 * God-Activity. Failures are silent (UI keeps prior state), same as before.
 * Every function <= 30 lines.
 */
class CommentsViewModel : ViewModel() {

    private val _commentList = MutableStateFlow<List<CommentItem>>(emptyList())
    val commentList: StateFlow<List<CommentItem>> = _commentList.asStateFlow()
    private val _commentsLoading = MutableStateFlow(false)
    val commentsLoading: StateFlow<Boolean> = _commentsLoading.asStateFlow()
    private val _commentsHasMore = MutableStateFlow(false)
    val commentsHasMore: StateFlow<Boolean> = _commentsHasMore.asStateFlow()
    private val _commentsLoadingMore = MutableStateFlow(false)
    val commentsLoadingMore: StateFlow<Boolean> = _commentsLoadingMore.asStateFlow()

    fun loadComments(url: String?) {
        _commentsLoading.value = true
        viewModelScope.launch {
            try {
                _commentList.value = YouTubeRepo.comments(url)
                _commentsHasMore.value = YouTubeRepo.CommentPager.hasMore
            } catch (_: Exception) { }
            _commentsLoading.value = false
        }
    }

    fun loadMoreComments() {
        if (_commentsLoadingMore.value || !_commentsHasMore.value) return
        _commentsLoadingMore.value = true
        viewModelScope.launch {
            try {
                val more = YouTubeRepo.CommentPager.more()
                if (more.isNotEmpty()) _commentList.value = _commentList.value + more
                _commentsHasMore.value = YouTubeRepo.CommentPager.hasMore
            } catch (_: Exception) {
                _commentsHasMore.value = false
            } finally {
                _commentsLoadingMore.value = false
            }
        }
    }
}
