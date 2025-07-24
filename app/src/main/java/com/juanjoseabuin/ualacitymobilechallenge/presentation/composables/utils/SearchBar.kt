package com.juanjoseabuin.ualacitymobilechallenge.presentation.composables.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DarkBlue
import com.juanjoseabuin.ualacitymobilechallenge.presentation.theme.DesertWhite

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable () -> Unit = {},
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = DarkBlue
            )
        },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DarkBlue,
            unfocusedTextColor = DarkBlue,
            focusedContainerColor = DesertWhite,
            unfocusedContainerColor = DesertWhite,
            cursorColor = DarkBlue,
            focusedLeadingIconColor = DarkBlue,
            unfocusedLeadingIconColor = DarkBlue,
            focusedTrailingIconColor = DarkBlue,
            unfocusedTrailingIconColor = DarkBlue,
            focusedLabelColor = DarkBlue,
            unfocusedLabelColor = DarkBlue.copy(alpha = 0.7f),
            focusedPlaceholderColor = DarkBlue.copy(alpha = 0.5f),
            unfocusedPlaceholderColor = DarkBlue.copy(alpha = 0.5f),
        )
    )
}