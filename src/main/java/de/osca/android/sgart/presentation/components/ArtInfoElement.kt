package de.osca.android.sgart.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.osca.android.essentials.presentation.component.design.BaseCardContainer
import de.osca.android.essentials.presentation.component.design.MasterDesignArgs
import de.osca.android.essentials.presentation.component.design.ModuleDesignArgs
import de.osca.android.sgart.entity.KunstInWald

@Composable
fun ArtInfoElement(
    masterDesignArgs: MasterDesignArgs,
    moduleDesignArgs: ModuleDesignArgs,
    kunstInWald: KunstInWald,
    distance: String,
    onClick: () -> Unit
) {
    BaseCardContainer(
        masterDesignArgs = masterDesignArgs,
        moduleDesignArgs = moduleDesignArgs,
        useContentPadding = false,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(
                        moduleDesignArgs.mCardContentPadding ?: masterDesignArgs.mCardContentPadding
                    )
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = kunstInWald.KUENSTLER ?: "",
                        style = masterDesignArgs.overlineTextStyle,
                        color = moduleDesignArgs.mCardTextColor ?: masterDesignArgs.mCardTextColor,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = kunstInWald.KURZTEXT ?: "",
                        style = masterDesignArgs.normalTextStyle,
                        color = moduleDesignArgs.mCardTextColor ?: masterDesignArgs.mCardTextColor,
                        textAlign = TextAlign.Start,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier
                        .height(8.dp)
                    )

                    Text(
                        text = "${distance}m",
                        style = masterDesignArgs.normalTextStyle,
                        color = masterDesignArgs.highlightColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}