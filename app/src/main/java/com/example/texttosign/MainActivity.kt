package com.example.texttosign

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.compose.ui.res.painterResource
import com.example.texttosign.ui.theme.TextToSignTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextToSignTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedOption by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE4E1).copy(alpha = 0.9f))
    ) {
        when (selectedOption) {
            "Text to Sign" -> SentenceImageView { selectedOption = "" }
            "Sign to Text" -> SignToTextScreen { selectedOption = "" }
            else -> OptionSelectionScreen { selectedOption = it }
        }
    }
}

@Composable
fun OptionSelectionScreen(onOptionSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Select an Option",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFC6471),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onOptionSelected("Text to Sign") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC75F))
        ) {
            Text("Text to Sign", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onOptionSelected("Sign to Text") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA5D0))
        ) {
            Text("Sign to Text", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun SentenceImageView(onBack: () -> Unit) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var videoResource by remember { mutableStateOf(0) }
    var images by remember { mutableStateOf<List<Int>>(emptyList()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = textState,
            onValueChange = { newText -> textState = newText },
            label = { Text("Enter a sentence") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                videoResource = 0 // Reset the video resource
                images = emptyList() // Reset images list

                val sentence = textState.text.trim().lowercase()
                val sentenceWithUnderscores = sentence.replace(" ", "_")

                videoResource = context.resources.getIdentifier(
                    sentenceWithUnderscores,
                    "raw",
                    context.packageName
                )

                if (videoResource == 0) {
                    val words = sentence.split(" ")
                    images = words.mapNotNull { word ->
                        val resourceId =
                            context.resources.getIdentifier(word, "drawable", context.packageName)
                        if (resourceId != 0) resourceId else null
                    }
                }
            },
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC75F))
        ) {
            Text("Show Video or Images", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (videoResource != 0) {
            // Use a key to force reinitialization of VideoView
            val videoKey = remember(videoResource) { videoResource }

            AndroidView(
                factory = { context ->
                    VideoView(context).apply {
                        val videoUri =
                            Uri.parse("android.resource://" + context.packageName + "/" + videoKey)
                        setVideoURI(videoUri)
                        start()
                    }
                },
                modifier = Modifier
                    .size(400.dp)
                    .padding(8.dp),
                update = { videoView ->
                    val videoUri =
                        Uri.parse("android.resource://" + context.packageName + "/" + videoKey)
                    videoView.setVideoURI(videoUri)
                    videoView.start()
                }
            )
        } else if (images.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                for (imageResId in images) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(300.dp) // Increase image size
                            .padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onBack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC6471))
        ) {
            Text("Back", fontSize = 16.sp, color = Color.White)
        }
    }
}


@Composable
fun SignToTextScreen(onBack: () -> Unit) {
    var resultText by remember { mutableStateOf("") }
    val context = LocalContext.current

    var imageUriState by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUriState != null) {
            sendSignRecognitionRequest(
                imageUri = imageUriState!!,
                context = context,
                onSuccess = { response -> resultText = "Recognition result: $response" },
                onError = { error -> resultText = "Error: $error" }
            )
        } else {
            resultText = "Image capture failed."
        }
    }

    val createTempImageUri = {
        val photoFile = File(context.cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Sign to Text",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFA5D0),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val tempUri = createTempImageUri()
                imageUriState = tempUri
                takePictureLauncher.launch(tempUri)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC75F))
        ) {
            Text("Capture Photo", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (resultText.isNotEmpty()) {
            Text(resultText, fontSize = 16.sp, color = Color(0xFF333333))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onBack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFC6471))
        ) {
            Text("Back", fontSize = 16.sp, color = Color.White)
        }
    }
}

fun sendSignRecognitionRequest(
    imageUri: Uri,
    context: Context,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val resolver = context.contentResolver
    val inputStream = resolver.openInputStream(imageUri)

    if (inputStream == null) {
        onError("InputStream is null for URI: $imageUri")
        return
    }

    val file = File(context.cacheDir, "captured_image.jpg")
    try {
        FileOutputStream(file).use { outputStream ->
            inputStream.use { input -> input.copyTo(outputStream) }
        }

        if (!file.exists() || file.length() == 0L) {
            onError("File save failed or file is empty")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaType()))
            .build()

        val request = Request.Builder()
            .url("http://192.168.184.198:8000/upload")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { onSuccess(it) }
                } else {
                    onError("Error: ${response.message}")
                }
            }
        })

    } catch (e: IOException) {
        onError("File operation failed: ${e.message}")
    }
}
