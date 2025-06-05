package com.smartbot.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Manages camera operations for computer vision tasks
 */
class CameraManager(private val context: Context) {
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var camera: Camera? = null
    
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()
    
    data class CameraState(
        val isInitialized: Boolean = false,
        val isAnalyzing: Boolean = false,
        val currentResolution: String = "",
        val frameRate: Int = 0,
        val error: String? = null,
        val lastFrameTimestamp: Long = 0L
    )
    
    /**
     * Initialize camera
     */
    fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: androidx.camera.view.PreviewView,
        onFrameAnalyzed: (Bitmap) -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // Set up preview
                preview = Preview.Builder()
                    .setTargetResolution(android.util.Size(640, 480))
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // Set up image analysis
                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(android.util.Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImage(imageProxy, onFrameAnalyzed)
                        }
                    }
                
                // Select camera (back camera preferred)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Bind use cases to camera
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                
                _cameraState.value = _cameraState.value.copy(
                    isInitialized = true,
                    currentResolution = "640x480",
                    error = null
                )
                
            } catch (e: Exception) {
                _cameraState.value = _cameraState.value.copy(
                    isInitialized = false,
                    error = "Camera initialization failed: ${e.message}"
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * Start image analysis
     */
    fun startAnalysis() {
        _cameraState.value = _cameraState.value.copy(isAnalyzing = true)
    }
    
    /**
     * Stop image analysis
     */
    fun stopAnalysis() {
        _cameraState.value = _cameraState.value.copy(isAnalyzing = false)
    }
    
    /**
     * Process camera image
     */
    private fun processImage(imageProxy: ImageProxy, onFrameAnalyzed: (Bitmap) -> Unit) {
        if (!_cameraState.value.isAnalyzing) {
            imageProxy.close()
            return
        }
        
        try {
            val bitmap = imageProxyToBitmap(imageProxy)
            bitmap?.let { bmp ->
                _cameraState.value = _cameraState.value.copy(
                    lastFrameTimestamp = System.currentTimeMillis()
                )
                onFrameAnalyzed(bmp)
            }
        } catch (e: Exception) {
            _cameraState.value = _cameraState.value.copy(
                error = "Image processing error: ${e.message}"
            )
        } finally {
            imageProxy.close()
        }
    }
    
    /**
     * Convert ImageProxy to Bitmap
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            // Try YUV to RGB conversion for other formats
            try {
                val yBuffer = imageProxy.planes[0].buffer
                val uBuffer = imageProxy.planes[1].buffer
                val vBuffer = imageProxy.planes[2].buffer
                
                val ySize = yBuffer.remaining()
                val uSize = uBuffer.remaining()
                val vSize = vBuffer.remaining()
                
                val nv21 = ByteArray(ySize + uSize + vSize)
                
                yBuffer.get(nv21, 0, ySize)
                vBuffer.get(nv21, ySize, vSize)
                uBuffer.get(nv21, ySize + vSize, uSize)
                
                val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
                val out = ByteArrayOutputStream()
                yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
                val imageBytes = out.toByteArray()
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e2: Exception) {
                null
            }
        }
    }
    
    /**
     * Capture still image
     */
    fun captureImage(onImageCaptured: (Bitmap?) -> Unit) {
        if (!_cameraState.value.isInitialized) {
            onImageCaptured(null)
            return
        }
        
        // For simplicity, we'll capture from the current analysis frame
        // In a full implementation, you'd use ImageCapture use case
        val currentTime = System.currentTimeMillis()
        _cameraState.value = _cameraState.value.copy(lastFrameTimestamp = currentTime)
        
        // This would be implemented with ImageCapture in a full version
        onImageCaptured(null)
    }
    
    /**
     * Toggle camera (front/back)
     */
    fun toggleCamera(lifecycleOwner: LifecycleOwner, previewView: androidx.camera.view.PreviewView) {
        // Implementation would switch between front and back cameras
        // For now, just reinitialize with the other camera
    }
    
    /**
     * Set camera resolution
     */
    fun setResolution(width: Int, height: Int, lifecycleOwner: LifecycleOwner, previewView: androidx.camera.view.PreviewView) {
        try {
            cameraProvider?.unbindAll()
            
            preview = Preview.Builder()
                .setTargetResolution(android.util.Size(width, height))
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(width, height))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            
            _cameraState.value = _cameraState.value.copy(
                currentResolution = "${width}x${height}"
            )
            
        } catch (e: Exception) {
            _cameraState.value = _cameraState.value.copy(
                error = "Resolution change failed: ${e.message}"
            )
        }
    }
    
    /**
     * Get available camera resolutions
     */
    fun getAvailableResolutions(): List<Pair<Int, Int>> {
        return listOf(
            Pair(320, 240),   // QVGA
            Pair(640, 480),   // VGA
            Pair(800, 600),   // SVGA
            Pair(1024, 768),  // XGA
            Pair(1280, 720),  // HD
            Pair(1920, 1080)  // Full HD
        )
    }
    
    /**
     * Enable/disable torch
     */
    fun setTorchEnabled(enabled: Boolean) {
        try {
            camera?.cameraControl?.enableTorch(enabled)
        } catch (e: Exception) {
            _cameraState.value = _cameraState.value.copy(
                error = "Torch control failed: ${e.message}"
            )
        }
    }
    
    /**
     * Set zoom level
     */
    fun setZoomLevel(zoomRatio: Float) {
        try {
            camera?.cameraControl?.setZoomRatio(zoomRatio.coerceIn(1f, 10f))
        } catch (e: Exception) {
            _cameraState.value = _cameraState.value.copy(
                error = "Zoom control failed: ${e.message}"
            )
        }
    }
    
    /**
     * Get camera capabilities
     */
    fun getCameraCapabilities(): Map<String, Any> {
        return try {
            val cameraInfo = camera?.cameraInfo
            mapOf(
                "hasFlashUnit" to (cameraInfo?.hasFlashUnit() ?: false),
                "zoomRatio" to (cameraInfo?.zoomState?.value?.zoomRatio ?: 1f),
                "maxZoomRatio" to (cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f),
                "minZoomRatio" to (cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f),
                "torchState" to (cameraInfo?.torchState?.value ?: 0)
            )
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    /**
     * Clean up camera resources
     */
    fun cleanup() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        _cameraState.value = CameraState()
    }
}