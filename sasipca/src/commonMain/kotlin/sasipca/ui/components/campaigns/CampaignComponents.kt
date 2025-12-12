package sasipca.ui.components.campaigns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import sasipca.models.Campaign
import sasipca.ui.components.LoadingWidget
import java.time.LocalDate

fun Campaign.isActive(): Boolean {
    return try {
        val start = LocalDate.parse(this.startDate)
        val end = LocalDate.parse(this.endDate)
        val now = LocalDate.now()
        !now.isBefore(start) && !now.isAfter(end)
    } catch (e: Exception) {
        false
    }
}

@Composable
fun CampaignsGrid(
    campaigns: List<Campaign>,
    isLoading: Boolean,
    onCampaignClick: (Campaign) -> Unit
) {
    if (isLoading) {
        LoadingWidget()
    } else if (campaigns.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Nenhuma campanha encontrada.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(campaigns) { campaign ->
                CampaignCard(campaign, onClick = { onCampaignClick(campaign) })
            }
        }
    }
}

@Composable
fun CampaignCard(
    campaign: Campaign,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp), // Elevação ligeira para destacar do fundo branco
        shape = RoundedCornerShape(12.dp), // Igual ao arredondamento da barra de pesquisa
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Cartão branco/surface
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Área da Imagem
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!campaign.imageUrl.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = campaign.imageUrl,
                        contentDescription = "Imagem da campanha ${campaign.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        error = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.BrokenImage,
                                    contentDescription = "Erro ao carregar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }

                StatusBadge(
                    isActive = campaign.isActive(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            // 2. Informações
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = campaign.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface // Cor de texto padrão
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = campaign.description ?: "Sem descrição",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, // Texto secundário
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${campaign.startDate} a ${campaign.endDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(isActive: Boolean, modifier: Modifier = Modifier) {
    // Mantive as cores fixas para garantir o significado semântico (verde/vermelho) independentemente do tema
    val containerColor = if (isActive) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
    val contentColor = if (isActive) Color(0xFF1E8E3E) else Color(0xFFC5221F)
    val text = if (isActive) "ATIVA" else "INATIVA"

    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}