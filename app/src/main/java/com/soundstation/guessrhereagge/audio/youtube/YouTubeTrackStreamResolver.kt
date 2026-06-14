package com.soundstation.guessrhereagge.audio.youtube

import android.util.Log
import com.soundstation.guessrhereagge.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.VideoStream

class YouTubeTrackStreamResolver {

    private val videoIdCache = mutableMapOf<String, String>()

    suspend fun resolve(track: Track): ResolvedStream = withContext(Dispatchers.IO) {
        try {
            NewPipeHolder.ensureInitialized()
            val videoId = resolveVideoId(track)
            val stream = extractPlayableStream(videoId)
            if (stream.url.isBlank()) {
                throw StreamResolutionException("Empty stream URL for ${track.artist} - ${track.title}")
            }
            Log.d(TAG, "Resolved ${track.title} -> ${stream.url.take(80)}...")
            stream
        } catch (error: StreamResolutionException) {
            throw error
        } catch (error: Exception) {
            Log.e(TAG, "Resolve failed for ${track.artist} - ${track.title}", error)
            throw StreamResolutionException(
                "Failed to resolve ${track.artist} - ${track.title}: ${error.message}",
                error,
            )
        }
    }

    fun invalidate(trackId: String) {
        videoIdCache.remove(trackId)
    }

    private fun resolveVideoId(track: Track): String {
        track.youtubeVideoId?.takeIf { it.isNotBlank() }?.let { return it }
        videoIdCache[track.id]?.let { return it }

        val videoId = searchVideoId(track)
        videoIdCache[track.id] = videoId
        return videoId
    }

    private fun searchVideoId(track: Track): String {
        val query = buildSearchQuery(track)
        val searchExtractor = ServiceList.YouTube.getSearchExtractor(
            query,
            listOf("music_songs", "videos"),
            "",
        )
        searchExtractor.fetchPage()

        val candidates = searchExtractor.initialPage.items
            .filterIsInstance<StreamInfoItem>()
            .filter { item -> item.streamType == StreamType.VIDEO_STREAM }

        val match = candidates.firstOrNull { item -> matchesTrack(item, track) }
            ?: candidates.firstOrNull()
            ?: throw StreamResolutionException("No YouTube match for ${track.artist} - ${track.title}")

        return videoIdFromItem(match)
    }

    private fun extractPlayableStream(videoId: String): ResolvedStream {
        val linkHandler = ServiceList.YouTube.streamLHFactory.fromId(videoId)
        val extractor = ServiceList.YouTube.getStreamExtractor(linkHandler)
        extractor.fetchPage()

        extractor.dashMpdUrl?.takeIf { it.isNotBlank() }?.let { url ->
            return ResolvedStream(url, defaultStreamHeaders())
        }

        extractor.hlsUrl?.takeIf { it.isNotBlank() }?.let { url ->
            return ResolvedStream(url, defaultStreamHeaders())
        }

        selectAudioStream(extractor)?.let { audio ->
            return ResolvedStream(
                url = audio.content,
                requestHeaders = defaultStreamHeaders(),
            )
        }

        selectVideoStream(extractor)?.let { video ->
            return ResolvedStream(
                url = video.content,
                requestHeaders = defaultStreamHeaders(),
            )
        }

        throw StreamResolutionException("No playable stream for video $videoId")
    }

    private fun selectAudioStream(extractor: StreamExtractor): AudioStream? =
        extractor.audioStreams
            .asSequence()
            .filter { stream -> stream.content.isNotBlank() }
            .sortedWith(
                compareByDescending<AudioStream> { stream -> stream.averageBitrate }
                    .thenBy { stream -> if (stream.format?.mimeType?.contains("mp4") == true) 0 else 1 },
            )
            .firstOrNull()

    private fun selectVideoStream(extractor: StreamExtractor): VideoStream? =
        extractor.videoStreams
            .asSequence()
            .filter { stream -> stream.content.isNotBlank() && !stream.isVideoOnly }
            .sortedBy { stream -> stream.height }
            .firstOrNull()

    private fun defaultStreamHeaders(): Map<String, String> = mapOf(
        "User-Agent" to USER_AGENT,
        "Referer" to "https://www.youtube.com",
        "Origin" to "https://www.youtube.com",
    )

    private fun buildSearchQuery(track: Track): String =
        track.youtubeSearchQuery?.takeIf { it.isNotBlank() }
            ?: "${track.artist} ${track.title}"

    private fun matchesTrack(item: StreamInfoItem, track: Track): Boolean {
        val haystack = "${item.name} ${item.uploaderName}".lowercase()
        val titleTokens = track.title
            .lowercase()
            .split(Regex("[^a-z0-9]+"))
            .filter { token -> token.length >= 3 }
        if (titleTokens.isEmpty()) return true
        val matchedTokens = titleTokens.count { token -> token in haystack }
        return matchedTokens >= (titleTokens.size / 2).coerceAtLeast(1)
    }

    private fun videoIdFromItem(item: StreamInfoItem): String =
        extractVideoIdFromUrl(item.url)

    private fun extractVideoIdFromUrl(url: String): String {
        Regex("[?&]v=([\\w-]{11})").find(url)?.groupValues?.get(1)?.let { return it }
        Regex("youtu\\.be/([\\w-]{11})").find(url)?.groupValues?.get(1)?.let { return it }
        Regex("/shorts/([\\w-]{11})").find(url)?.groupValues?.get(1)?.let { return it }
        throw StreamResolutionException("Could not parse video id from $url")
    }

    companion object {
        private const val TAG = "GuessrAudio"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}
