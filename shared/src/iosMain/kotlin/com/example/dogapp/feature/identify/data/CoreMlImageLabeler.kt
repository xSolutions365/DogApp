package com.example.dogapp.feature.identify.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.identify.domain.IdentifyError
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.CoreML.MLModel
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.create
import platform.ImageIO.CGImagePropertyOrientation
import platform.ImageIO.kCGImagePropertyOrientationDown
import platform.ImageIO.kCGImagePropertyOrientationDownMirrored
import platform.ImageIO.kCGImagePropertyOrientationLeft
import platform.ImageIO.kCGImagePropertyOrientationLeftMirrored
import platform.ImageIO.kCGImagePropertyOrientationRight
import platform.ImageIO.kCGImagePropertyOrientationRightMirrored
import platform.ImageIO.kCGImagePropertyOrientationUp
import platform.ImageIO.kCGImagePropertyOrientationUpMirrored
import platform.UIKit.UIImage
import platform.UIKit.UIImageOrientation
import platform.Vision.VNClassificationObservation
import platform.Vision.VNCoreMLModel
import platform.Vision.VNCoreMLRequest
import platform.Vision.VNImageCropAndScaleOptionCenterCrop
import platform.Vision.VNImageRequestHandler

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class CoreMlImageLabeler : ImageLabeler {
    private val mutex = Mutex()
    private var model: VNCoreMLModel? = null

    override suspend fun label(photo: ByteArray): Outcome<Map<String, Float>, IdentifyError> =
        withContext(Dispatchers.Default) {
            val image = UIImage.imageWithData(photo.toNSData())
            val cgImage = image?.CGImage ?: return@withContext Outcome.Failure(IdentifyError.UnreadablePhoto)
            mutex.withLock {
                val visionModel =
                    model ?: loadModel()?.also { model = it } ?: return@withLock Outcome.Failure(IdentifyError.ModelUnavailable)
                val request = VNCoreMLRequest(model = visionModel).apply { imageCropAndScaleOption = VNImageCropAndScaleOptionCenterCrop }
                val handler =
                    VNImageRequestHandler(
                        cGImage = cgImage,
                        orientation = image.imageOrientation.toPropertyOrientation(),
                        options = emptyMap<Any?, Any?>(),
                    )
                if (!handler.performRequests(listOf(request), error = null)) return@withLock Outcome.Failure(IdentifyError.UnreadablePhoto)
                val observations = request.results.orEmpty().filterIsInstance<VNClassificationObservation>()
                Outcome.Success(observations.associate { it.identifier to it.confidence })
            }
        }

    private fun loadModel(): VNCoreMLModel? {
        val url = NSBundle.mainBundle.URLForResource(MODEL_NAME, withExtension = COMPILED_MODEL_EXTENSION) ?: return null
        val mlModel = MLModel.modelWithContentsOfURL(url, error = null) ?: return null
        return VNCoreMLModel.modelForMLModel(mlModel, error = null)
    }

    private fun ByteArray.toNSData(): NSData =
        if (isEmpty()) NSData() else usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) }

    private fun UIImageOrientation.toPropertyOrientation(): CGImagePropertyOrientation =
        when (this) {
            UIImageOrientation.UIImageOrientationDown -> kCGImagePropertyOrientationDown
            UIImageOrientation.UIImageOrientationLeft -> kCGImagePropertyOrientationLeft
            UIImageOrientation.UIImageOrientationRight -> kCGImagePropertyOrientationRight
            UIImageOrientation.UIImageOrientationUpMirrored -> kCGImagePropertyOrientationUpMirrored
            UIImageOrientation.UIImageOrientationDownMirrored -> kCGImagePropertyOrientationDownMirrored
            UIImageOrientation.UIImageOrientationLeftMirrored -> kCGImagePropertyOrientationLeftMirrored
            UIImageOrientation.UIImageOrientationRightMirrored -> kCGImagePropertyOrientationRightMirrored
            else -> kCGImagePropertyOrientationUp
        }

    private companion object {
        const val MODEL_NAME = "MobileNetV2"
        const val COMPILED_MODEL_EXTENSION = "mlmodelc"
    }
}
