package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun SvgFromUrlImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    // Create an ImageLoader instance with SVG decoder
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "SVG Image from URL",
        modifier = modifier.size(32.dp)
    )
}