package com.unistack.app.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.theme.UniStackColors

@Composable
fun UniStackLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Image(
        painter = painterResource(id = R.drawable.unistack_option_a_symbol),
        contentDescription = "UniStack",
        modifier = modifier.size(size)
    )
}

@Composable
fun UniStackLogoMarkWhite(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Image(
        painter = painterResource(id = R.drawable.unistack_option_a_symbol_white),
        contentDescription = "UniStack",
        modifier = modifier.size(size)
    )
}

@Composable
fun UniStackBrandHeader(
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    symbolSize: Dp = 34.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniStackLogoMark(size = symbolSize)
        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = UniStackColors.TextPrimary)) {
                        append("Uni")
                    }
                    withStyle(SpanStyle(color = UniStackColors.Primary)) {
                        append("Stack")
                    }
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 23.sp,
                    lineHeight = 27.sp,
                    letterSpacing = 0.sp
                ),
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
