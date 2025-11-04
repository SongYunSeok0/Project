package com.mypage.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.common.design.R

@androidx.compose.runtime.Composable
fun FAQScreen(modifier: androidx.compose.ui.Modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion) {
    _root_ide_package_.androidx.compose.foundation.layout.Box(
        modifier = modifier
            .requiredWidth(width = 412.dp)
            .requiredHeight(height = 917.dp)
            .background(color = _root_ide_package_.androidx.compose.ui.graphics.Color(0xfffcfcfc))
    ) {
        _root_ide_package_.androidx.compose.foundation.layout.Box(
            modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                .requiredWidth(width = 364.dp)
                .requiredHeight(height = 49.dp)
        ) {
            _root_ide_package_.androidx.compose.foundation.layout.Column(
                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                    .requiredWidth(width = 182.dp)
                    .requiredHeight(height = 50.dp)
                    .padding(
                        start = 37.29166793823242.dp,
                        end = 37.29166793823242.dp,
                        top = 12.825515747070312.dp,
                        bottom = 1.6666699647903442.dp
                    )
            ) {
                _root_ide_package_.androidx.compose.foundation.layout.Row(
                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                        .fillMaxWidth()
                        .requiredHeight(height = 22.dp)
                ) {
                    _root_ide_package_.androidx.compose.material3.Text(
                        text = "자주 묻는 질문",
                        color = _root_ide_package_.androidx.compose.ui.graphics.Color(0xff6ae0d9),
                        textAlign = _root_ide_package_.androidx.compose.ui.text.style.TextAlign.Companion.Center,
                        lineHeight = 1.5.em,
                        style = _root_ide_package_.androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = _root_ide_package_.androidx.compose.ui.text.font.FontWeight.Companion.Bold
                        ),
                        modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                            .fillMaxWidth()
                    )
                }
            }
            _root_ide_package_.androidx.compose.foundation.layout.Column(
                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                    .align(alignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.TopStart)
                    .offset(
                        x = 181.84.dp,
                        y = 0.dp
                    )
                    .requiredWidth(width = 182.dp)
                    .requiredHeight(height = 49.dp)
                    .padding(
                        start = 44.7265625.dp,
                        end = 44.739585876464844.dp,
                        top = 13.1640625.dp
                    )
            ) {
                _root_ide_package_.androidx.compose.foundation.layout.Row(
                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                        .fillMaxWidth()
                        .requiredHeight(height = 22.dp)
                ) {
                    _root_ide_package_.androidx.compose.material3.Text(
                        text = "1:1 문의하기",
                        color = _root_ide_package_.androidx.compose.ui.graphics.Color(0xff666666),
                        textAlign = _root_ide_package_.androidx.compose.ui.text.style.TextAlign.Companion.Center,
                        lineHeight = 1.5.em,
                        style = _root_ide_package_.androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            fontWeight = _root_ide_package_.androidx.compose.ui.text.font.FontWeight.Companion.Bold
                        ),
                        modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                            .fillMaxWidth()
                    )
                }
            }
        }
        _root_ide_package_.androidx.compose.foundation.layout.Column(
            verticalArrangement = _root_ide_package_.androidx.compose.foundation.layout.Arrangement.spacedBy(
                24.dp,
                _root_ide_package_.androidx.compose.ui.Alignment.Companion.Top
            ),
            modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                .requiredWidth(width = 412.dp)
                .requiredHeight(height = 793.dp)
                .padding(
                    start = 23.997394561767578.dp,
                    end = 23.997386932373047.dp,
                    top = 23.997390747070312.dp
                )
        ) {
            _root_ide_package_.androidx.compose.foundation.layout.Row(
                horizontalArrangement = _root_ide_package_.androidx.compose.foundation.layout.Arrangement.spacedBy(
                    11.99.dp,
                    _root_ide_package_.androidx.compose.ui.Alignment.Companion.Start
                ),
                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                    .fillMaxWidth()
                    .requiredHeight(height = 77.dp)
                    .clip(
                        shape = _root_ide_package_.androidx.compose.foundation.shape.RoundedCornerShape(
                            14.dp
                        )
                    )
                    .background(
                        brush = _root_ide_package_.androidx.compose.ui.graphics.Brush.Companion.linearGradient(
                            0f to _root_ide_package_.androidx.compose.ui.graphics.Color(0xffe4f5f4),
                            1f to _root_ide_package_.androidx.compose.ui.graphics.Color(0xffd5f0ee),
                            start = _root_ide_package_.androidx.compose.ui.geometry.Offset(
                                181.84f,
                                0f
                            ),
                            end = _root_ide_package_.androidx.compose.ui.geometry.Offset(
                                181.84f,
                                77.45f
                            )
                        )
                    )
                    .padding(
                        start = 15.989582061767578.dp,
                        top = 15.989578247070312.dp
                    )
            ) {
                _root_ide_package_.androidx.compose.foundation.Image(
                    painter = _root_ide_package_.androidx.compose.ui.res.painterResource(id = _root_ide_package_.com.common.design.R.drawable.pill),
                    contentDescription = "Icon",
                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                        .requiredSize(size = 20.dp)
                )
                _root_ide_package_.androidx.compose.foundation.layout.Box(
                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                        .requiredWidth(width = 248.dp)
                        .requiredHeight(height = 45.dp)
                ) {
                    _root_ide_package_.androidx.compose.material3.Text(
                        text = "궁금한 사항이 있으신가요?",
                        color = _root_ide_package_.androidx.compose.ui.graphics.Color(0xff5db0a8),
                        lineHeight = 1.63.em,
                        style = _root_ide_package_.androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp
                        ),
                        modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                            .align(alignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.TopStart)
                            .offset(
                                x = 0.dp,
                                y = (-1.33).dp
                            )
                    )
                    _root_ide_package_.androidx.compose.material3.Text(
                        text = "자주 묻는 질문에서 답변을 찾아보세요!",
                        color = _root_ide_package_.androidx.compose.ui.graphics.Color(0xff5db0a8),
                        lineHeight = 1.63.em,
                        style = _root_ide_package_.androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp
                        ),
                        modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                            .align(alignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.TopStart)
                            .offset(
                                x = 0.dp,
                                y = 21.4.dp
                            )
                    )
                }
            }
            _root_ide_package_.androidx.compose.foundation.layout.Box(
                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                    .fillMaxWidth()
                    .requiredHeight(height = 336.dp)
            ) {
                _root_ide_package_.androidx.compose.foundation.layout.Column(
                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                        .requiredWidth(width = 364.dp)
                        .requiredHeight(height = 58.dp)
                        .clip(
                            shape = _root_ide_package_.androidx.compose.foundation.shape.RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(color = _root_ide_package_.androidx.compose.ui.graphics.Color.Companion.White)
                        .border(
                            border = _root_ide_package_.androidx.compose.foundation.BorderStroke(
                                0.8333330154418945.dp,
                                _root_ide_package_.androidx.compose.ui.graphics.Color(0xfff3f4f6)
                            ),
                            shape = _root_ide_package_.androidx.compose.foundation.shape.RoundedCornerShape(
                                14.dp
                            )
                        )
                        .padding(
                            start = 0.8333339691162109.dp,
                            end = 0.8333530426025391.dp,
                            top = 0.8333282470703125.dp,
                            bottom = 0.8333330154418945.dp
                        )
                ) {
                    _root_ide_package_.androidx.compose.foundation.layout.Row(
                        horizontalArrangement = _root_ide_package_.androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.CenterVertically,
                        modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                            .fillMaxWidth()
                            .requiredHeight(height = 56.dp)
                            .padding(
                                start = 15.989583969116211.dp,
                                end = 15.989572525024414.dp
                            )
                    ) {
                        _root_ide_package_.androidx.compose.foundation.layout.Row(
                            horizontalArrangement = _root_ide_package_.androidx.compose.foundation.layout.Arrangement.spacedBy(
                                11.99.dp,
                                _root_ide_package_.androidx.compose.ui.Alignment.Companion.Start
                            ),
                            modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                .requiredHeight(height = 24.dp)
                                .weight(weight = 1f)
                        ) {
                            _root_ide_package_.androidx.compose.foundation.Image(
                                painter = _root_ide_package_.androidx.compose.ui.res.painterResource(
                                    id = _root_ide_package_.com.common.design.R.drawable.pill
                                ),
                                contentDescription = "Icon",
                                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                    .requiredSize(size = 20.dp)
                            )
                            _root_ide_package_.androidx.compose.foundation.layout.Box(
                                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                    .requiredWidth(width = 177.dp)
                                    .requiredHeight(height = 24.dp)
                            ) {
                                _root_ide_package_.androidx.compose.material3.Text(
                                    text = "복약 알림이 오지 않아요",
                                    color = _root_ide_package_.androidx.compose.ui.graphics.Color(
                                        0xff101828
                                    ),
                                    lineHeight = 1.5.em,
                                    style = _root_ide_package_.androidx.compose.ui.text.TextStyle(
                                        fontSize = 16.sp
                                    ),
                                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                        .align(alignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.TopStart)
                                        .offset(
                                            x = 0.dp,
                                            y = (-2.17).dp
                                        )
                                )
                            }
                        }
                        _root_ide_package_.androidx.compose.foundation.Image(
                            painter = _root_ide_package_.androidx.compose.ui.res.painterResource(id = _root_ide_package_.com.common.design.R.drawable.pill),
                            contentDescription = "Icon",
                            modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                .requiredSize(size = 20.dp)
                        )
                    }
                }
                _root_ide_package_.androidx.compose.foundation.layout.Column(
                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                        .align(alignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.TopStart)
                        .offset(
                            x = 0.dp,
                            y = 69.64.dp
                        )
                        .requiredWidth(width = 364.dp)
                        .requiredHeight(height = 58.dp)
                        .clip(
                            shape = _root_ide_package_.androidx.compose.foundation.shape.RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(color = _root_ide_package_.androidx.compose.ui.graphics.Color.Companion.White)
                        .border(
                            border = _root_ide_package_.androidx.compose.foundation.BorderStroke(
                                0.8333330154418945.dp,
                                _root_ide_package_.androidx.compose.ui.graphics.Color(0xfff3f4f6)
                            ),
                            shape = _root_ide_package_.androidx.compose.foundation.shape.RoundedCornerShape(
                                14.dp
                            )
                        )
                        .padding(
                            start = 0.8333339691162109.dp,
                            end = 0.8333530426025391.dp,
                            top = 0.83331298828125.dp,
                            bottom = 0.8333330154418945.dp
                        )
                ) {
                    _root_ide_package_.androidx.compose.foundation.layout.Row(
                        horizontalArrangement = _root_ide_package_.androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.CenterVertically,
                        modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                            .fillMaxWidth()
                            .requiredHeight(height = 56.dp)
                            .padding(
                                start = 15.989583969116211.dp,
                                end = 15.989572525024414.dp
                            )
                    ) {
                        _root_ide_package_.androidx.compose.foundation.layout.Row(
                            horizontalArrangement = _root_ide_package_.androidx.compose.foundation.layout.Arrangement.spacedBy(
                                11.99.dp,
                                _root_ide_package_.androidx.compose.ui.Alignment.Companion.Start
                            ),
                            modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                .requiredHeight(height = 24.dp)
                                .weight(weight = 1f)
                        ) {
                            _root_ide_package_.androidx.compose.foundation.Image(
                                painter = _root_ide_package_.androidx.compose.ui.res.painterResource(
                                    id = _root_ide_package_.com.common.design.R.drawable.pill
                                ),
                                contentDescription = "Icon",
                                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                    .requiredSize(size = 20.dp)
                            )
                            _root_ide_package_.androidx.compose.foundation.layout.Box(
                                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                    .requiredWidth(width = 177.dp)
                                    .requiredHeight(height = 24.dp)
                            ) {
                                _root_ide_package_.androidx.compose.material3.Text(
                                    text = "처방전 인식이 잘 안돼요",
                                    color = _root_ide_package_.androidx.compose.ui.graphics.Color(
                                        0xff101828
                                    ),
                                    lineHeight = 1.5.em,
                                    style = _root_ide_package_.androidx.compose.ui.text.TextStyle(
                                        fontSize = 16.sp
                                    ),
                                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                        .align(alignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.TopStart)
                                        .offset(
                                            x = 0.dp,
                                            y = (-2.17).dp
                                        )
                                )
                            }
                        }
                        _root_ide_package_.androidx.compose.foundation.Image(
                            painter = _root_ide_package_.androidx.compose.ui.res.painterResource(id = _root_ide_package_.com.common.design.R.drawable.pill),
                            contentDescription = "Icon",
                            modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                .requiredSize(size = 20.dp)
                        )
                    }
                }
                _root_ide_package_.androidx.compose.foundation.layout.Column(
                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                        .align(alignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.TopStart)
                        .offset(
                            x = 0.dp,
                            y = 139.27.dp
                        )
                        .requiredWidth(width = 364.dp)
                        .requiredHeight(height = 58.dp)
                        .clip(
                            shape = _root_ide_package_.androidx.compose.foundation.shape.RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(color = _root_ide_package_.androidx.compose.ui.graphics.Color.Companion.White)
                        .border(
                            border = _root_ide_package_.androidx.compose.foundation.BorderStroke(
                                0.8333330154418945.dp,
                                _root_ide_package_.androidx.compose.ui.graphics.Color(0xfff3f4f6)
                            ),
                            shape = _root_ide_package_.androidx.compose.foundation.shape.RoundedCornerShape(
                                14.dp
                            )
                        )
                        .padding(
                            start = 0.8333339691162109.dp,
                            end = 0.8333530426025391.dp,
                            top = 0.833343505859375.dp,
                            bottom = 0.8333330154418945.dp
                        )
                ) {
                    _root_ide_package_.androidx.compose.foundation.layout.Row(
                        horizontalArrangement = _root_ide_package_.androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = _root_ide_package_.androidx.compose.ui.Alignment.Companion.CenterVertically,
                        modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                            .fillMaxWidth()
                            .requiredHeight(height = 56.dp)
                            .padding(
                                start = 15.989583969116211.dp,
                                end = 15.989572525024414.dp
                            )
                    ) {
                        _root_ide_package_.androidx.compose.foundation.layout.Row(
                            horizontalArrangement = _root_ide_package_.androidx.compose.foundation.layout.Arrangement.spacedBy(
                                11.99.dp,
                                _root_ide_package_.androidx.compose.ui.Alignment.Companion.Start
                            ),
                            modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                .requiredHeight(height = 24.dp)
                                .weight(weight = 1f)
                        ) {
                            _root_ide_package_.androidx.compose.foundation.Image(
                                painter = _root_ide_package_.androidx.compose.ui.res.painterResource(
                                    id = _root_ide_package_.com.common.design.R.drawable.pill
                                ),
                                contentDescription = "Icon",
                                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                    .requiredSize(size = 20.dp)
                            )
                            _root_ide_package_.androidx.compose.foundation.layout.Box(
                                modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                    .requiredWidth(width = 225.dp)
                                    .requiredHeight(height = 24.dp)
                            ) {
                                _root_ide_package_.androidx.compose.material3.Text(
                                    text = "심박수 측정이 정확하지 않아요",
                                    color = _root_ide_package_.androidx.compose.ui.graphics.Color(
                                        0xff101828
                                    ),
                                    lineHeight = 1.5.em,
                                    style = _root_ide_package_.androidx.compose.ui.text.TextStyle(
                                        fontSize = 16.sp
                                    ),
                                    modifier = _root_ide_package_.androidx.compose.ui.Modifier.Companion
                                        .align(alignment = Alignment.TopStart)
                                        .offset(
                                            x = 0.dp,
                                            y = (-2.17).dp
                                        )
                                )
                            }
                        }
                        Image(
                            painter = painterResource(id = R.drawable.pill),
                            contentDescription = "Icon",
                            modifier = Modifier
                                .requiredSize(size = 20.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 0.dp,
                            y = 208.91.dp
                        )
                        .requiredWidth(width = 364.dp)
                        .requiredHeight(height = 58.dp)
                        .clip(shape = RoundedCornerShape(14.dp))
                        .background(color = Color.White)
                        .border(
                            border = BorderStroke(0.8333330154418945.dp, Color(0xfff3f4f6)),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(
                            start = 0.8333339691162109.dp,
                            end = 0.8333530426025391.dp,
                            top = 0.833343505859375.dp,
                            bottom = 0.8333330154418945.dp
                        )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 56.dp)
                            .padding(
                                start = 15.989583969116211.dp,
                                end = 15.989572525024414.dp
                            )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.Start),
                            modifier = Modifier
                                .requiredHeight(height = 24.dp)
                                .weight(weight = 1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pill),
                                contentDescription = "Icon",
                                modifier = Modifier
                                    .requiredSize(size = 20.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .requiredWidth(width = 225.dp)
                                    .requiredHeight(height = 24.dp)
                            ) {
                                Text(
                                    text = "프로필 사진을 변경하고 싶어요",
                                    color = Color(0xff101828),
                                    lineHeight = 1.5.em,
                                    style = TextStyle(
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier
                                        .align(alignment = Alignment.TopStart)
                                        .offset(
                                            x = 0.dp,
                                            y = (-2.17).dp
                                        )
                                )
                            }
                        }
                        Image(
                            painter = painterResource(id = R.drawable.pill),
                            contentDescription = "Icon",
                            modifier = Modifier
                                .requiredSize(size = 20.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 0.dp,
                            y = 278.54.dp
                        )
                        .requiredWidth(width = 364.dp)
                        .requiredHeight(height = 58.dp)
                        .clip(shape = RoundedCornerShape(14.dp))
                        .background(color = Color.White)
                        .border(
                            border = BorderStroke(0.8333330154418945.dp, Color(0xfff3f4f6)),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(
                            start = 0.8333339691162109.dp,
                            end = 0.8333530426025391.dp,
                            top = 0.83331298828125.dp,
                            bottom = 0.8333330154418945.dp
                        )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 56.dp)
                            .padding(
                                start = 15.989583969116211.dp,
                                end = 15.989572525024414.dp
                            )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(11.99.dp, Alignment.Start),
                            modifier = Modifier
                                .requiredHeight(height = 24.dp)
                                .weight(weight = 1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pill),
                                contentDescription = "Icon",
                                modifier = Modifier
                                    .requiredSize(size = 20.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .requiredWidth(width = 209.dp)
                                    .requiredHeight(height = 24.dp)
                            ) {
                                Text(
                                    text = "복약 기록을 삭제하고 싶어요",
                                    color = Color(0xff101828),
                                    lineHeight = 1.5.em,
                                    style = TextStyle(
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier
                                        .align(alignment = Alignment.TopStart)
                                        .offset(
                                            x = 0.dp,
                                            y = (-2.17).dp
                                        )
                                )
                            }
                        }
                        Image(
                            painter = painterResource(id = R.drawable.pill),
                            contentDescription = "Icon",
                            modifier = Modifier
                                .requiredSize(size = 20.dp)
                        )
                    }
                }
            }
        }

    }
    Box(
        modifier = Modifier
            .offset(x = 0.dp,
                y = 44.dp)
            .requiredWidth(width = 412.dp)
            .requiredHeight(height = 873.dp)
            .background(color = Color(0xfffcfcfc))
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 156.52.dp,
                    y = 19.dp)
                .requiredWidth(width = 81.dp)
                .requiredHeight(height = 23.dp)
                .background(color = Color(0xffb5e5e1).copy(alpha = 0.36f))
        ) {
            Text(
                text = "마이페이지",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 16.sp,
                    letterSpacing = 1.sp),
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 0.99.dp,
                        y = 3.33.dp))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(15.99.dp, Alignment.Top),
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 0.dp,
                    y = 57.99.dp)
                .requiredWidth(width = 393.dp)
                .requiredHeight(height = 294.dp)
                .padding(start = 23.997394561767578.dp,
                    end = 23.997417449951172.dp,
                    top = 23.997390747070312.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.99.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(height = 90.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .requiredSize(size = 90.dp)
                        .clip(shape = RoundedCornerShape(27962000.dp))
                        .background(color = Color(0xffffb7c5))
                        .shadow(elevation = 4.dp,
                            shape = RoundedCornerShape(27962000.dp))
                ) {
                    Text(
                        text = "😊",
                        color = Color(0xff0a0a0a),
                        lineHeight = 1.em,
                        style = TextStyle(
                            fontSize = 48.sp))
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(7.99.dp, Alignment.Top),
                    modifier = Modifier
                        .requiredWidth(width = 70.dp)
                        .requiredHeight(height = 52.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 20.dp)
                    ) {
                        Text(
                            text = "안녕하세요",
                            color = Color(0xff221f1f),
                            lineHeight = 1.43.em,
                            style = TextStyle(
                                fontSize = 14.sp),
                            modifier = Modifier
                                .fillMaxWidth())
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(height = 24.dp)
                    ) {
                        Text(
                            text = "김이름님",
                            color = Color(0xff221f1f),
                            lineHeight = 1.5.em,
                            style = TextStyle(
                                fontSize = 16.sp),
                            modifier = Modifier
                                .align(alignment = Alignment.TopStart)
                                .offset(x = 0.dp,
                                    y = (-2.17).dp)
                                .requiredWidth(width = 65.dp))
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(height = 116.dp)
                    .clip(shape = RoundedCornerShape(14.dp))
                    .background(color = Color.White)
                    .padding(start = 15.989582061767578.dp,
                        end = 15.989574432373047.dp,
                        top = 15.989593505859375.dp)
                    .shadow(elevation = 4.dp,
                        shape = RoundedCornerShape(14.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(height = 84.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.99.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 13.65.dp,
                                y = 0.dp)
                            .requiredWidth(width = 67.dp)
                            .requiredHeight(height = 84.dp)
                            .padding(bottom = -0.0000019073486328125.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .requiredSize(size = 32.dp)
                                .padding(start = 0.000003814697265625.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pill),
                                contentDescription = "Icon",
                                modifier = Modifier
                                    .requiredSize(size = 24.dp))
                        }
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 67.dp)
                                .requiredHeight(height = 28.dp)
                        ) {
                            Text(
                                text = "215bpm",
                                color = Color(0xff6ae0d9),
                                lineHeight = 1.56.em,
                                style = TextStyle(
                                    fontSize = 18.sp),
                                modifier = Modifier
                                    .align(alignment = Alignment.TopStart)
                                    .offset(x = 0.dp,
                                        y = (-1.33).dp))
                        }
                        Row(
                            modifier = Modifier
                                .requiredWidth(width = 55.dp)
                                .requiredHeight(height = 16.dp)
                        ) {
                            Text(
                                text = "Heart rate",
                                color = Color(0xff5db0a8).copy(alpha = 0.74f),
                                lineHeight = 1.33.em,
                                style = TextStyle(
                                    fontSize = 12.sp))
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.99.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 136.43.dp,
                                y = 0.dp)
                            .requiredWidth(width = 54.dp)
                            .requiredHeight(height = 84.dp)
                            .padding(bottom = -0.0000019073486328125.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .requiredSize(size = 32.dp)
                                .padding(end = 0.000011444091796875.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pill),
                                contentDescription = "Icon",
                                modifier = Modifier
                                    .requiredSize(size = 24.dp))
                        }
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 54.dp)
                                .requiredHeight(height = 28.dp)
                        ) {
                            Text(
                                text = "170cm",
                                color = Color(0xff6ae0d9),
                                lineHeight = 1.56.em,
                                style = TextStyle(
                                    fontSize = 18.sp),
                                modifier = Modifier
                                    .align(alignment = Alignment.TopStart)
                                    .offset(x = 0.dp,
                                        y = (-1.33).dp))
                        }
                        Row(
                            modifier = Modifier
                                .requiredWidth(width = 36.dp)
                                .requiredHeight(height = 16.dp)
                        ) {
                            Text(
                                text = "Height",
                                color = Color(0xff5db0a8).copy(alpha = 0.74f),
                                lineHeight = 1.33.em,
                                style = TextStyle(
                                    fontSize = 12.sp))
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.99.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 246.09.dp,
                                y = 0.dp)
                            .requiredWidth(width = 53.dp)
                            .requiredHeight(height = 84.dp)
                            .padding(bottom = -0.0000019073486328125.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .requiredSize(size = 32.dp)
                                .padding(start = 0.000019073486328125.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pill),
                                contentDescription = "Icon",
                                modifier = Modifier
                                    .requiredSize(size = 24.dp))
                        }
                        Box(
                            modifier = Modifier
                                .requiredWidth(width = 53.dp)
                                .requiredHeight(height = 28.dp)
                        ) {
                            Text(
                                text = "103lbs",
                                color = Color(0xff6ae0d9),
                                lineHeight = 1.56.em,
                                style = TextStyle(
                                    fontSize = 18.sp),
                                modifier = Modifier
                                    .align(alignment = Alignment.TopStart)
                                    .offset(x = 0.dp,
                                        y = (-1.33).dp))
                        }
                        Row(
                            modifier = Modifier
                                .requiredWidth(width = 39.dp)
                                .requiredHeight(height = 16.dp)
                        ) {
                            Text(
                                text = "Weight",
                                color = Color(0xff5db0a8).copy(alpha = 0.74f),
                                lineHeight = 1.33.em,
                                style = TextStyle(
                                    fontSize = 12.sp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 108.15.dp,
                                y = 19.99.dp)
                            .requiredWidth(width = 1.dp)
                            .requiredHeight(height = 44.dp)
                            .background(color = Color(0xff407ce2).copy(alpha = 0.13f)))
                    Box(
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 217.81.dp,
                                y = 19.99.dp)
                            .requiredWidth(width = 1.dp)
                            .requiredHeight(height = 44.dp)
                            .background(color = Color(0xff407ce2).copy(alpha = 0.13f)))
                }
            }
        }
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 0.dp,
                    y = 351.93.dp)
                .requiredWidth(width = 393.dp)
                .requiredHeight(height = 459.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 0.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 1.dp)
                    .background(color = Color(0xff407ce2).copy(alpha = 0.13f)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.99.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 17.19.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 84.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .requiredWidth(width = 48.dp)
                        .requiredHeight(height = 52.dp)
                        .clip(shape = RoundedCornerShape(27962000.dp))
                        .background(color = Color(0xff407ce2).copy(alpha = 0.13f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pill),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .requiredSize(size = 24.dp))
                }
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp)
                ) {
                    Text(
                        text = "내 정보 수정",
                        color = Color(0xff221f1f),
                        lineHeight = 1.5.em,
                        style = TextStyle(
                            fontSize = 16.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 0.dp,
                                y = (-2.17).dp))
                }
                Image(
                    painter = painterResource(id = R.drawable.pill),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .requiredSize(size = 20.dp))
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 101.21.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 1.dp)
                    .background(color = Color(0xff407ce2).copy(alpha = 0.13f)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.99.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 102.41.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 84.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .requiredWidth(width = 48.dp)
                        .requiredHeight(height = 52.dp)
                        .clip(shape = RoundedCornerShape(27962000.dp))
                        .background(color = Color(0xff407ce2).copy(alpha = 0.13f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pill),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .requiredSize(size = 24.dp))
                }
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp)
                ) {
                    Text(
                        text = "심박수",
                        color = Color(0xff221f1f),
                        lineHeight = 1.5.em,
                        style = TextStyle(
                            fontSize = 16.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 0.dp,
                                y = (-2.17).dp))
                }
                Image(
                    painter = painterResource(id = R.drawable.pill),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .requiredSize(size = 20.dp))
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 186.43.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 1.dp)
                    .background(color = Color(0xff407ce2).copy(alpha = 0.13f)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.99.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 187.63.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 84.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .requiredWidth(width = 48.dp)
                        .requiredHeight(height = 52.dp)
                        .clip(shape = RoundedCornerShape(27962000.dp))
                        .background(color = Color(0xff407ce2).copy(alpha = 0.13f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pill),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .requiredSize(size = 24.dp))
                }
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp)
                ) {
                    Text(
                        text = "복약 그래프",
                        color = Color(0xff221f1f),
                        lineHeight = 1.5.em,
                        style = TextStyle(
                            fontSize = 16.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 0.dp,
                                y = (-2.17).dp))
                }
                Image(
                    painter = painterResource(id = R.drawable.pill),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .requiredSize(size = 20.dp))
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 271.65.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 1.dp)
                    .background(color = Color(0xff407ce2).copy(alpha = 0.13f)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.99.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 272.85.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 84.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .requiredWidth(width = 48.dp)
                        .requiredHeight(height = 52.dp)
                        .clip(shape = RoundedCornerShape(27962000.dp))
                        .background(color = Color(0xff407ce2).copy(alpha = 0.13f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pill),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .requiredSize(size = 24.dp))
                }
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp)
                ) {
                    Text(
                        text = "FAQ 문의사항",
                        color = Color(0xff221f1f),
                        lineHeight = 1.5.em,
                        style = TextStyle(
                            fontSize = 16.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 0.dp,
                                y = (-2.17).dp))
                }
                Image(
                    painter = painterResource(id = R.drawable.pill),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .requiredSize(size = 20.dp))
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 356.88.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 1.dp)
                    .background(color = Color(0xff407ce2).copy(alpha = 0.13f)))
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.99.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 358.07.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 84.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .requiredWidth(width = 48.dp)
                        .requiredHeight(height = 52.dp)
                        .clip(shape = RoundedCornerShape(27962000.dp))
                        .background(color = Color(0xff407ce2).copy(alpha = 0.13f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pill),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .requiredSize(size = 24.dp))
                }
                Box(
                    modifier = Modifier
                        .requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp)
                ) {
                    Text(
                        text = "로그아웃",
                        color = Color(0xff221f1f),
                        lineHeight = 1.5.em,
                        style = TextStyle(
                            fontSize = 16.sp),
                        modifier = Modifier
                            .align(alignment = Alignment.TopStart)
                            .offset(x = 0.dp,
                                y = (-2.17).dp))
                }
                Image(
                    painter = painterResource(id = R.drawable.pill),
                    contentDescription = "Icon",
                    modifier = Modifier
                        .requiredSize(size = 20.dp))
            }
            Box(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 24.dp,
                        y = 458.09.dp)
                    .requiredWidth(width = 345.dp)
                    .requiredHeight(height = 1.dp)
                    .background(color = Color(0xff407ce2).copy(alpha = 0.13f)))
        }

    }
}

@Preview(widthDp = 412, heightDp = 917)
@Composable
private fun AppPreview() {
    FAQScreen(Modifier)
}