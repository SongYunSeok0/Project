package com.mypage.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil3.compose.rememberAsyncImagePainter
import com.shared.R

@Composable
fun ImageAttachmentSection(
    images: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onImageRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    placeholderBg: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,

) {
    val attachImage = stringResource(R.string.attachimage)
    val addText = stringResource(R.string.add)
    val attachedImages = stringResource(R.string.attachedimages)
    val ImagesText = stringResource(R.string.images)
    val deleteImage = stringResource(R.string.deleteimage)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newImages = (images + uris).take(3)
            onImagesSelected(newImages)
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = attachImage,
                style = MaterialTheme.typography.labelLarge,
                color = textColor
                )
            Text(
                text = "${images.size}/3",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 이미지 추가 버튼
            if (images.size < 3) {
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(shape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.camera),
                                contentDescription = attachImage,
                                tint = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = addText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // 선택된 이미지들
            itemsIndexed(images) { index, uri ->
                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    // coil 의존성 추가 필요해서 임시 블록처리 -> 1201 추가,
                    // 이미지첨부는되는데 서버로전달은안됨
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "$attachedImages ${index + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape),
                        contentScale = ContentScale.Crop
                    )

                    /*// 코일 대신 ui보는 용도 임시코드
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$ImagesText ${index + 1}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }*/

                    // 삭제 버튼
                    /*IconButton(
                        onClick = { onImageRemove(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .offset(x = (-4).dp, y = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = shape
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.x_delete), // X 아이콘으로 교체
                            contentDescription = deleteImage,
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize()
                        )
                    }*/
                    // 삭제 버튼 (IconButton → Box로 교체)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .size(20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = shape
                            )
                            .clickable { onImageRemove(index) }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.x_delete),
                            contentDescription = deleteImage,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}