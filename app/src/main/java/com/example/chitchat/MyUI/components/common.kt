package com.example.chitchat.MyUI.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.example.chitchat.R

@Composable
fun commonImage(data : String, modifier : Modifier){
    val painter: Painter = if (data.isNotEmpty()) {
        // Placeholder image while loading
        rememberAsyncImagePainter(ImageRequest.Builder // Image to show in case of loading error
            (LocalContext.current).data(data = data).apply<ImageRequest.Builder>(block = fun ImageRequest.Builder.() {
            transformations(CircleCropTransformation())
            scale(Scale.FILL)
            placeholder(R.drawable.person_image) // Placeholder image while loading
            error(R.drawable.person_image) // Image to show in case of loading error
        }).build()
        )
    } else {
        painterResource(id = R.drawable.person_image)
    }

    Image(
        painter = painter, contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.FillBounds
    )
}