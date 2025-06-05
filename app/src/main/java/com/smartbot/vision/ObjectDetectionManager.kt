package com.smartbot.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages computer vision tasks using ML Kit
 */
class ObjectDetectionManager(private val context: Context) {
    
    // ML Kit detectors
    private val objectDetector: ObjectDetector
    private val faceDetector: FaceDetector
    private val textRecognizer: TextRecognizer
    
    private val _detectionResults = MutableStateFlow(DetectionResults())
    val detectionResults: StateFlow<DetectionResults> = _detectionResults.asStateFlow()
    
    data class DetectionResults(
        val objects: List<DetectedObject> = emptyList(),
        val faces: List<DetectedFace> = emptyList(),
        val texts: List<DetectedText> = emptyList(),
        val processingTime: Long = 0L,
        val error: String? = null,
        val lastProcessedTimestamp: Long = 0L
    )
    
    data class DetectedObject(
        val boundingBox: Rect,
        val trackingId: Int?,
        val labels: List<ObjectLabel>,
        val confidence: Float
    )
    
    data class ObjectLabel(
        val text: String,
        val confidence: Float,
        val index: Int
    )
    
    data class DetectedFace(
        val boundingBox: Rect,
        val trackingId: Int?,
        val landmarks: Map<String, android.graphics.PointF>,
        val rotationY: Float,
        val rotationZ: Float,
        val smilingProbability: Float?,
        val leftEyeOpenProbability: Float?,
        val rightEyeOpenProbability: Float?
    )
    
    data class DetectedText(
        val text: String,
        val boundingBox: Rect,
        val confidence: Float,
        val language: String?
    )
    
    init {
        // Configure object detector
        val objectDetectorOptions = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .enableMultipleObjects()
            .build()
        objectDetector = ObjectDetection.getClient(objectDetectorOptions)
        
        // Configure face detector
        val faceDetectorOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
        faceDetector = FaceDetection.getClient(faceDetectorOptions)
        
        // Configure text recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    
    /**
     * Process image for all detection types
     */
    fun processImage(bitmap: Bitmap, enabledDetections: Set<DetectionType> = setOf(DetectionType.OBJECTS)) {
        val startTime = System.currentTimeMillis()
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        
        var objectsResult: List<DetectedObject> = emptyList()
        var facesResult: List<DetectedFace> = emptyList()
        var textsResult: List<DetectedText> = emptyList()
        
        var completedTasks = 0
        val totalTasks = enabledDetections.size
        
        fun checkCompletion() {
            completedTasks++
            if (completedTasks >= totalTasks) {
                val processingTime = System.currentTimeMillis() - startTime
                _detectionResults.value = DetectionResults(
                    objects = objectsResult,
                    faces = facesResult,
                    texts = textsResult,
                    processingTime = processingTime,
                    lastProcessedTimestamp = System.currentTimeMillis(),
                    error = null
                )
            }
        }
        
        // Object detection
        if (DetectionType.OBJECTS in enabledDetections) {
            objectDetector.process(inputImage)
                .addOnSuccessListener { objects ->
                    objectsResult = objects.map { obj ->
                        DetectedObject(
                            boundingBox = obj.boundingBox,
                            trackingId = obj.trackingId,
                            labels = obj.labels.map { label ->
                                ObjectLabel(
                                    text = label.text,
                                    confidence = label.confidence,
                                    index = label.index
                                )
                            },
                            confidence = obj.labels.maxOfOrNull { it.confidence } ?: 0f
                        )
                    }
                    checkCompletion()
                }
                .addOnFailureListener { e ->
                    _detectionResults.value = _detectionResults.value.copy(
                        error = "Object detection failed: ${e.message}"
                    )
                    checkCompletion()
                }
        }
        
        // Face detection
        if (DetectionType.FACES in enabledDetections) {
            faceDetector.process(inputImage)
                .addOnSuccessListener { faces ->
                    facesResult = faces.map { face ->
                        val landmarks = mutableMapOf<String, android.graphics.PointF>()
                        
                        face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)?.position?.let {
                            landmarks["left_eye"] = it
                        }
                        face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)?.position?.let {
                            landmarks["right_eye"] = it
                        }
                        face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.NOSE_BASE)?.position?.let {
                            landmarks["nose"] = it
                        }
                        face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_MOUTH)?.position?.let {
                            landmarks["left_mouth"] = it
                        }
                        face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_MOUTH)?.position?.let {
                            landmarks["right_mouth"] = it
                        }
                        
                        DetectedFace(
                            boundingBox = face.boundingBox,
                            trackingId = face.trackingId,
                            landmarks = landmarks,
                            rotationY = face.headEulerAngleY,
                            rotationZ = face.headEulerAngleZ,
                            smilingProbability = face.smilingProbability,
                            leftEyeOpenProbability = face.leftEyeOpenProbability,
                            rightEyeOpenProbability = face.rightEyeOpenProbability
                        )
                    }
                    checkCompletion()
                }
                .addOnFailureListener { e ->
                    _detectionResults.value = _detectionResults.value.copy(
                        error = "Face detection failed: ${e.message}"
                    )
                    checkCompletion()
                }
        }
        
        // Text recognition
        if (DetectionType.TEXT in enabledDetections) {
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    textsResult = visionText.textBlocks.flatMap { block ->
                        block.lines.map { line ->
                            DetectedText(
                                text = line.text,
                                boundingBox = line.boundingBox ?: Rect(),
                                confidence = line.confidence ?: 0f,
                                language = line.recognizedLanguage
                            )
                        }
                    }
                    checkCompletion()
                }
                .addOnFailureListener { e ->
                    _detectionResults.value = _detectionResults.value.copy(
                        error = "Text recognition failed: ${e.message}"
                    )
                    checkCompletion()
                }
        }
        
        // If no detections enabled, complete immediately
        if (enabledDetections.isEmpty()) {
            _detectionResults.value = DetectionResults(
                processingTime = System.currentTimeMillis() - startTime,
                lastProcessedTimestamp = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Get educational descriptions for detected objects
     */
    fun getObjectDescription(objectLabel: String): String {
        return when (objectLabel.lowercase()) {
            "person" -> "A human being. Humans are mammals and the most intelligent species on Earth."
            "car", "vehicle" -> "A motor vehicle used for transportation. Cars have engines and wheels."
            "dog" -> "A domesticated mammal and popular pet. Dogs are known for their loyalty."
            "cat" -> "A small domesticated carnivorous mammal. Cats are independent and agile."
            "bird" -> "A warm-blooded vertebrate with feathers and wings. Most birds can fly."
            "book" -> "A written or printed work consisting of pages. Books contain knowledge and stories."
            "chair" -> "A piece of furniture for sitting. Chairs typically have a back and four legs."
            "table" -> "A piece of furniture with a flat top and legs. Tables are used for eating or working."
            "phone", "cell phone" -> "A device for communication over long distances using radio waves."
            "computer", "laptop" -> "An electronic device for processing data and running programs."
            "tree" -> "A woody plant with a trunk, branches, and leaves. Trees produce oxygen."
            "flower" -> "The reproductive structure of flowering plants. Flowers are often colorful."
            "ball" -> "A round object used in sports and games. Balls can bounce and roll."
            "bicycle" -> "A two-wheeled vehicle powered by pedaling. Bicycles are eco-friendly transport."
            "apple" -> "A round fruit that grows on trees. Apples are nutritious and sweet."
            else -> "An object detected in the image. Each object has unique properties and uses."
        }
    }
    
    /**
     * Get face analysis description
     */
    fun getFaceAnalysis(face: DetectedFace): String {
        val analysis = mutableListOf<String>()
        
        face.smilingProbability?.let { prob ->
            if (prob > 0.7f) analysis.add("smiling")
            else if (prob < 0.3f) analysis.add("not smiling")
        }
        
        face.leftEyeOpenProbability?.let { leftProb ->
            face.rightEyeOpenProbability?.let { rightProb ->
                when {
                    leftProb > 0.8f && rightProb > 0.8f -> analysis.add("eyes open")
                    leftProb < 0.2f && rightProb < 0.2f -> analysis.add("eyes closed")
                    leftProb < 0.2f || rightProb < 0.2f -> analysis.add("winking")
                }
            }
        }
        
        when {
            face.rotationY > 15f -> analysis.add("looking right")
            face.rotationY < -15f -> analysis.add("looking left")
            else -> analysis.add("looking forward")
        }
        
        when {
            face.rotationZ > 15f -> analysis.add("head tilted right")
            face.rotationZ < -15f -> analysis.add("head tilted left")
        }
        
        return if (analysis.isNotEmpty()) {
            "Face detected: ${analysis.joinToString(", ")}"
        } else {
            "Face detected"
        }
    }
    
    /**
     * Get text analysis
     */
    fun getTextAnalysis(texts: List<DetectedText>): String {
        if (texts.isEmpty()) return "No text detected"
        
        val totalText = texts.joinToString(" ") { it.text }
        val wordCount = totalText.split("\\s+".toRegex()).size
        val avgConfidence = texts.map { it.confidence }.average()
        
        return "Text detected: $wordCount words with ${(avgConfidence * 100).toInt()}% confidence"
    }
    
    /**
     * Generate educational content based on detections
     */
    fun generateEducationalContent(results: DetectionResults): String {
        val content = mutableListOf<String>()
        
        // Objects
        if (results.objects.isNotEmpty()) {
            content.add("Objects found:")
            results.objects.forEach { obj ->
                obj.labels.firstOrNull()?.let { label ->
                    content.add("• ${label.text}: ${getObjectDescription(label.text)}")
                }
            }
        }
        
        // Faces
        if (results.faces.isNotEmpty()) {
            content.add("\nFaces found:")
            results.faces.forEach { face ->
                content.add("• ${getFaceAnalysis(face)}")
            }
        }
        
        // Text
        if (results.texts.isNotEmpty()) {
            content.add("\n${getTextAnalysis(results.texts)}")
        }
        
        return content.joinToString("\n")
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        objectDetector.close()
        faceDetector.close()
        textRecognizer.close()
    }
    
    enum class DetectionType {
        OBJECTS, FACES, TEXT
    }
}