package com.mohamedadel.mytube.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState

// H1: app-level PagingSource (no room-paging artifact): Room serves LIMIT/OFFSET pages,
// Paging 3 keeps the list lazy at any depth. Tabs = three instances with different queries.
class HistoryPaging(
    private val dao: PlaylistDao,
    private val tab: Int, // 0 all, 1 pinned, 2 unpinned
    private val keys: List<String>
) : PagingSource<Int, HistoryEntity>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HistoryEntity> {
        return try {
            val page = params.key ?: 0
            val size = params.loadSize
            // Empty pin set would emit IN () / NOT IN () — both invalid SQLite. Short-circuit.
            if ((tab == 1 || tab == 2) && keys.isEmpty()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }
            val rows = when (tab) {
                1 -> dao.historyPagePinned(keys, size, page * size)
                2 -> dao.historyPageUnpinned(keys, size, page * size)
                else -> dao.historyPage(size, page * size)
            }
            LoadResult.Page(
                data = rows,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (rows.size < size) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, HistoryEntity>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.let { (it.prevKey ?: -1) + 1 }
        }
    }
}
