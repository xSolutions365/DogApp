package com.example.dogapp.feature.identify.data

import android.content.Context
import android.graphics.Bitmap
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.size.Scale
import coil3.toBitmap
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.identify.domain.IdentifyError
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.LiteRtException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException

class LiteRtImageLabeler(
    private val context: Context,
) : ImageLabeler {
    private val mutex = Mutex()
    private var model: CompiledModel? = null
    private var labels: List<String>? = null

    override suspend fun label(photo: ByteArray): Outcome<Map<String, Float>, IdentifyError> {
        val bitmap = decode(photo) ?: return Outcome.Failure(IdentifyError.UnreadablePhoto)
        return withContext(Dispatchers.Default) {
            val input = bitmap.centerCrop().toModelInput()
            mutex.withLock {
                try {
                    Outcome.Success(loadLabels().zip(run(input).toList()).toMap())
                } catch (_: LiteRtException) {
                    Outcome.Failure(IdentifyError.ModelUnavailable)
                } catch (_: IOException) {
                    Outcome.Failure(IdentifyError.ModelUnavailable)
                }
            }
        }
    }

    private fun run(input: FloatArray): FloatArray {
        val compiledModel =
            model ?: CompiledModel.create(context.assets, MODEL_ASSET, CompiledModel.Options(Accelerator.CPU)).also { model = it }
        val inputBuffers = compiledModel.createInputBuffers()
        val outputBuffers = compiledModel.createOutputBuffers()
        try {
            inputBuffers.single().writeFloat(input)
            compiledModel.run(inputBuffers, outputBuffers)
            return outputBuffers.single().readFloat()
        } finally {
            (inputBuffers + outputBuffers).forEach { it.close() }
        }
    }

    private fun loadLabels(): List<String> =
        labels ?: context.assets
            .open(LABELS_ASSET)
            .bufferedReader()
            .use { it.readLines() }
            .also { labels = it }

    private suspend fun decode(photo: ByteArray): Bitmap? {
        val request =
            ImageRequest
                .Builder(context)
                .data(photo)
                .size(INPUT_SIZE)
                .scale(Scale.FILL)
                .precision(Precision.EXACT)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()
        val result = SingletonImageLoader.get(context).execute(request) as? SuccessResult
        return result?.image?.toBitmap()
    }

    private fun Bitmap.centerCrop(): Bitmap {
        val side = minOf(width, height)
        val cropped = Bitmap.createBitmap(this, (width - side) / 2, (height - side) / 2, side, side)
        return Bitmap.createScaledBitmap(cropped, INPUT_SIZE, INPUT_SIZE, true)
    }

    private fun Bitmap.toModelInput(): FloatArray {
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        val input = FloatArray(pixels.size * CHANNELS)
        pixels.forEachIndexed { index, pixel ->
            input[index * CHANNELS] = (pixel shr 16 and 0xFF).toChannel()
            input[index * CHANNELS + 1] = (pixel shr 8 and 0xFF).toChannel()
            input[index * CHANNELS + 2] = (pixel and 0xFF).toChannel()
        }
        return input
    }

    private fun Int.toChannel(): Float = this / PIXEL_MIDPOINT - 1f

    private companion object {
        const val MODEL_ASSET = "mobilenet_v2.tflite"
        const val LABELS_ASSET = "imagenet_labels.txt"
        const val INPUT_SIZE = 224
        const val CHANNELS = 3
        const val PIXEL_MIDPOINT = 127.5f
    }
}
