package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juanjoseabuin.ualacitymobilechallenge.R
import com.juanjoseabuin.ualacitymobilechallenge.presentation.model.CityUiItem
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DarkBlue
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DesertWhite

@Composable
fun CityCard(
    city: CityUiItem,
    onCardClick: (Long) -> Unit,
    onFavoriteIconClick: (Long) -> Unit,
    onDetailsButtonClick: (CityUiItem) -> Unit,
    isTogglingFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = { onCardClick(city.id) },
        colors = CardColors(
            containerColor = DesertWhite,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Box (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 4.dp),
                            text = city.fullName ?: "${city.name}, ${city.country}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Lat: ${city.coord.lat}, Lon: ${city.coord.lon}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkBlue.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onDetailsButtonClick(city) }
                    ) {
                        Text("Details")
                    }
                }
            }

            AnimatedFavoriteIcon(
                isFavorite = city.isFavorite,
                onClick = { onFavoriteIconClick(city.id) },
                isToggling = isTogglingFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp),
            )
        }
    }
}

private enum class FavoriteIconState { Filled, Outlined }

@Composable
fun BoxScope.AnimatedFavoriteIcon(
    isFavorite: Boolean,
    onClick: () -> Unit,
    isToggling: Boolean, // Added for loading indicator within the icon
    modifier: Modifier = Modifier
) {
    // --- Animation Logic for IconButton Icon ---
    // Determine the current state for the animation transition
    val favoriteIconState = if (isFavorite) FavoriteIconState.Filled else FavoriteIconState.Outlined

    // Create a transition based on the favoriteIconState
    val transition = updateTransition(favoriteIconState, label = "favoriteIconTransition")

    // Animate the scale of the icon
    val iconScale by transition.animateFloat(
        transitionSpec = {
            // Define the animation spec (e.g., spring for a bouncy effect)
            when {
                FavoriteIconState.Outlined isTransitioningTo FavoriteIconState.Filled ->
                    spring (dampingRatio = Spring .DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                else -> // For other transitions, e.g., filled to outlined
                    spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
            }
        },
        label = "iconScale"
    ) { state ->
        when (state) {
            FavoriteIconState.Filled -> 1.2f // Icon scales up when it becomes filled
            FavoriteIconState.Outlined -> 1.0f // Icon returns to normal size when outlined
        }
    }
    // --- End Animation Logic ---

    IconButton(
        onClick = onClick,
        modifier = modifier
            .align(Alignment.TopEnd)
            .padding(top = 8.dp, end = 8.dp)
    ) {
        Icon(
            modifier = Modifier
                .requiredSize(24.dp)
                .graphicsLayer(scaleX = iconScale, scaleY = iconScale), // Apply the animated scale here
            imageVector = ImageVector.vectorResource(if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined),
            contentDescription = "Favorite"
        )
    }
}