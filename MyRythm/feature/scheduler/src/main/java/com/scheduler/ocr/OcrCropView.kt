package com.scheduler.ocr

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class OcrCropView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : FrameLayout(ctx, attrs) {

    private val imageView = ImageView(ctx).apply {
        scaleType = ImageView.ScaleType.MATRIX
        adjustViewBounds = false
        setBackgroundColor(Color.TRANSPARENT)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    /** 드래그 선택 오버레이 */
    private val overlay = CropOverlay(ctx).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private var srcBitmap: Bitmap? = null

    // 문자열 결과 콜백
    private var onOcrDone: ((String) -> Unit)? = null
    // 구조 결과 콜백: (약품명, 횟수, 일수)
    private var onOcrParsed: ((List<Triple<String, Int?, Int?>>) -> Unit)? = null
    fun setOnOcrParsed(cb: (List<Triple<String, Int?, Int?>>) -> Unit) { onOcrParsed = cb }

    // 기본 비율 크롭
    private var cropTopPct = 0.06f
    private var cropBottomPct = 0.18f
    private var extraScale = 1.0f

    // 사용자 지정 박스(px)
    private var cropBoxImg: Rect? = null

    init {
        addView(imageView)
        addView(overlay) // 선택 영역 그리기
        viewTreeObserver.addOnGlobalLayoutListener { applyFillMatrix() }
    }

    /** 이미지 경로 바인딩 */
    fun bindImage(path: String) {
        srcBitmap = loadBitmapWithExif(path)
        val show = srcBitmap?.let { applyAutoCrop(it) }
        imageView.setImageBitmap(show)
        overlay.clear()
        post { applyFillMatrix() }
    }

    /** OCR 실행: 드래그 사각형이 있으면 그 영역만 인식 */
    fun runOcr(onDone: (String) -> Unit) {
        onOcrDone = onDone
        runOcrInternal()
    }

    /** CENTER_CROP 매트릭스 */
    private fun applyFillMatrix() {
        val bmp = (imageView.drawable as? BitmapDrawable)?.bitmap ?: return
        if (imageView.width == 0 || imageView.height == 0) return

        val vw = imageView.width.toFloat()
        val vh = imageView.height.toFloat()
        val bw = bmp.width.toFloat()
        val bh = bmp.height.toFloat()

        val sx = vw / bw
        val sy = vh / bh
        val baseScale = max(sx, sy)
        val scale = baseScale * extraScale

        val tx = (vw - bw * scale) / 2f
        val ty = (vh - bh * scale) / 2f

        Matrix().apply {
            setScale(scale, scale)
            postTranslate(tx, ty)
            imageView.imageMatrix = this
        }
    }

    /** EXIF 회전 보정 */
    private fun loadBitmapWithExif(path: String): Bitmap {
        val bmp = BitmapFactory.decodeFile(path)
        val exif = ExifInterface(path)
        val degree = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        if (degree == 0) return bmp
        val m = Matrix().apply { postRotate(degree.toFloat()) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    /** 자동 크롭 / 박스 크롭 */
    private fun applyAutoCrop(bmp: Bitmap): Bitmap {
        // 우선순위 1: 지정 박스
        cropBoxImg?.let { r ->
            val w = r.width().coerceAtLeast(1)
            val h = r.height().coerceAtLeast(1)
            return Bitmap.createBitmap(bmp, r.left, r.top, w, h)
        }
        // 우선순위 2: 비율 크롭
        if (cropTopPct == 0f && cropBottomPct == 0f) return bmp
        val top = (bmp.height * cropTopPct).toInt()
        val bottom = (bmp.height * cropBottomPct).toInt()
        val y = top.coerceAtLeast(0)
        val h = (bmp.height - top - bottom).coerceAtLeast(1)
        return Bitmap.createBitmap(bmp, 0, y, bmp.width, h)
    }

    // ---------------- OCR 처리 ----------------
    private data class OCRLine(val text: String, val rect: Rect)
    private data class OCRWord(val text: String, val rect: Rect)

    private fun yC(r: Rect) = (r.top + r.bottom) / 2
    private fun xC(r: Rect) = (r.left + r.right) / 2

    private fun normalizeNums(s: String) = s
        .replace('（', '(').replace('）', ')')
        .replace(Regex("(?<=\\d)\\s+(?=\\d)"), "")
        .replace(Regex("[lI|]"), "1")
        .replace('O', '0').replace('ㅇ', '0')
        .replace(Regex("\\s+"), " ")

    private fun withoutUnitsAndDecimals(s: String) = s
        .replace(Regex("\\b\\d+(?:\\.\\d+)?\\s*(mg|g|ml|%|밀리그램|그램|밀리리터)\\b", RegexOption.IGNORE_CASE), " ")
        .replace(Regex("\\b\\d+\\.\\d+\\b"), " ")

    @SuppressLint("SetTextI18n")
    private fun runOcrInternal() {
        val base = (imageView.drawable as? BitmapDrawable)?.bitmap ?: return

        // 드래그 선택 영역 확인 → 뷰좌표 → 비트맵좌표 변환
        val sel = overlay.getCropRect()
        if (sel == null) {
            onOcrDone?.invoke("영역을 드래그하세요")
            onOcrParsed?.invoke(emptyList())
            return
        }
        val inv = Matrix()
        imageView.imageMatrix.invert(inv)
        val pts = floatArrayOf(sel.left, sel.top, sel.right, sel.bottom)
        inv.mapPoints(pts)

        val lx = pts[0].toInt().coerceIn(0, base.width)
        val ty = pts[1].toInt().coerceIn(0, base.height)
        val rx = pts[2].toInt().coerceIn(0, base.width)
        val by = pts[3].toInt().coerceIn(0, base.height)
        val x = min(lx, rx)
        val y = min(ty, by)
        val w = max(1, abs(rx - lx))
        val h = max(1, abs(by - ty))

        if (w < 10 || h < 10) {
            onOcrDone?.invoke("선택 영역이 너무 작습니다")
            onOcrParsed?.invoke(emptyList())
            return
        }

        val crop = Bitmap.createBitmap(base, x, y, w, h)

        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        recognizer.process(InputImage.fromBitmap(crop, 0))
            .addOnSuccessListener { result ->
                val lines = result.textBlocks.flatMap { tb ->
                    tb.lines.mapNotNull { ln -> ln.boundingBox?.let { OCRLine(ln.text.trim(), it) } }
                }
                if (lines.isEmpty()) {
                    onOcrDone?.invoke("인식 결과 없음")
                    onOcrParsed?.invoke(emptyList())
                    return@addOnSuccessListener
                }

                val words = result.textBlocks.flatMap { tb ->
                    tb.lines.flatMap { ln ->
                        ln.elements.mapNotNull { el -> el.boundingBox?.let { OCRWord(el.text.trim(), it) } }
                    }
                }

                val sorted = lines.sortedBy { it.rect.top }
                val merged = mutableListOf<OCRLine>()
                var i = 0
                while (i < sorted.size) {
                    val cur = sorted[i]
                    val next = sorted.getOrNull(i + 1)
                    if (next != null && next.rect.top - cur.rect.bottom in 0..80 &&
                        (cur.text.endsWith("(") || cur.text.contains("정("))) {
                        merged += OCRLine(
                            cur.text + " " + next.text,
                            Rect(min(cur.rect.left, next.rect.left), cur.rect.top,
                                max(cur.rect.right, next.rect.right), next.rect.bottom)
                        )
                        i += 2
                    } else {
                        merged += cur; i++
                    }
                }

                val nameRight = (crop.width * 0.48f).toInt()
                val nameRows = merged.filter { it.rect.left < nameRight }.sortedBy { it.rect.top }
                val seen = HashSet<String>()
                val outList = mutableListOf<Triple<String, Int?, Int?>>()
                val sb = StringBuilder()

                for (row in nameRows) {
                    val name = extractDrug(row.text) ?: continue
                    if (!seen.add(name)) continue
                    val ymid = yC(row.rect)
                    val (times, days) = extractTimesDaysPerRowByTokens(words, ymid, nameRight, crop.width)
                    outList += Triple(name, times, days)
                    sb.append("$name | ${times ?: "-"}회 | ${days ?: "-"}일\n")
                }

                val outText = if (outList.isNotEmpty()) sb.toString().trimEnd() else "인식 결과 없음"
                onOcrDone?.invoke(outText)
                onOcrParsed?.invoke(outList)
            }
            .addOnFailureListener { onOcrDone?.invoke("인식 실패: ${it.message}") }
    }

    private fun extractDrug(line: String): String? {
        // --- 오탈자 보정 및 기호 정리 ---
        var s = line
            .replace('（','(').replace('）',')')
            .replace(Regex("[*•·]"), " ")
            // OCR 오탈자: 장용 → 장응/장옹/장융/장용 변형 교정
            .replace(Regex("장[옹용융응]정"), "장용정")
            .replace(Regex("장[옹용융응](?:캡슐|캅셀)"), "장용캡슐")

        // --- 불필요 괄호 제거 ---
        while (true) {
            val before = s
            s = s.replace(Regex("^\\s*\\([^)]{0,40}\\)\\s*"), " ")
            if (s == before) break
        }
        s = s.replace(Regex("\\([^)]{0,40}\\)"), " ").replace(Regex("\\s+"), "")

        // --- 기준 단어 세트 ---
        val colorWords = setOf(
            "분홍","분홍색","주황","주황색","흰","흰색","백","백색","담황","담황색","황","황색",
            "청","청색","회","회색","녹","녹색","남","남색","갈","갈색","붉은","붉은색","검은","검은색",
            "연노랑","연노랑색"
        )
        val shapeWords = setOf("원형","타원형","장방형","원")
        val formWords  = setOf("정","정제","서방정","장용정","장용캡슐","설하정","연질캡슐","경질캡슐","캡슐")
        val suffix = "(?:${formWords.joinToString("|")})"

        // --- 색상/형태만 있는 라인 차단 ---
        fun looksColorOrShapeOnly(z: String): Boolean {
            val cw = colorWords.joinToString("|")
            val sh = shapeWords.joinToString("|")
            val r1 = Regex("^($cw)(색)?$suffix$")
            val r2 = Regex("^($sh)$suffix$")
            val r3 = Regex("^($cw)(?:/|,|·)?($cw)?(색)?$suffix$")
            // 색상 + 서방/장용(오탈자 포함) + 제형 → 차단
            val r4 = Regex("^($cw)(색)?(서방|장[옹용융응])$suffix$")
            return r1.matches(z) || r2.matches(z) || r3.matches(z) || r4.matches(z)
        }

        // --- 약명 캡처 ---
        val m = Regex("([가-힣A-Za-z0-9·-]+?$suffix)").find(s) ?: return null
        val name = m.groupValues[1]
        if (looksColorOrShapeOnly(name)) return null

        val base = name.replace(Regex("$suffix$"), "")
        return if (base.length <= 1) null else name
    }

    private fun extractTimesDaysPerRowByTokens(
        words: List<OCRWord>, rowY: Int, nameRightX: Int, cropWidth: Int
    ): Pair<Int?, Int?> {
        val yWin = max(56, cropWidth / 30)
        val rowWords = words.filter { abs(yC(it.rect) - rowY) <= yWin }
        val margin = (cropWidth * 0.02f).toInt()
        val right = rowWords.filter { it.rect.left >= nameRightX - margin }
        val cands = right.flatMap { w ->
            val cleaned = withoutUnitsAndDecimals(normalizeNums(w.text))
            Regex("\\b([1-9]\\d?)\\b").findAll(cleaned).mapNotNull {
                it.groupValues[1].toIntOrNull()?.takeIf { v -> v in 1..60 }
            }.map { v -> v to xC(w.rect) }
        }.sortedBy { it.second }

        if (cands.isEmpty()) return null to null
        val distinct = mutableListOf<Pair<Int, Int>>()
        val tolX = (cropWidth * 0.03f).toInt()
        for (c in cands) {
            if (distinct.isEmpty() || abs(distinct.last().second - c.second) > tolX)
                distinct += c
        }

        val lastTwo = distinct.takeLast(2).map { it.first }
        val times: Int?
        val days: Int?
        if (lastTwo.size == 2) {
            val l = lastTwo[0]; val r = lastTwo[1]
            times = if (l in 1..6) l else null
            days = if (times == null && r in 1..6) l else r
        } else {
            times = null; days = lastTwo.first()
        }
        return times to days
    }
}

/** 드래그 영역 오버레이 */
private class CropOverlay(context: Context) : View(context) {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = 0xFFFFD54F.toInt()
    }
    private val shade = Paint().apply {
        color = 0x66000000
        style = Paint.Style.FILL
    }

    private var sx = 0f
    private var sy = 0f
    private var ex = 0f
    private var ey = 0f
    private var hasRect = false

    fun clear() { hasRect = false; invalidate() }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                sx = e.x; sy = e.y; ex = e.x; ey = e.y
                hasRect = true; invalidate(); performClick(); return true
            }
            MotionEvent.ACTION_MOVE -> { ex = e.x; ey = e.y; invalidate() }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { ex = e.x; ey = e.y; invalidate() }
        }
        return super.onTouchEvent(e)
    }
    override fun performClick(): Boolean { super.performClick(); return true }

    override fun onDraw(c: Canvas) {
        if (!hasRect) return
        val left = min(sx, ex); val top = min(sy, ey); val right = max(sx, ex); val bottom = max(sy, ey)
        // 바깥 음영
        c.drawRect(0f, 0f, width.toFloat(), top, shade)
        c.drawRect(0f, top, left, bottom, shade)
        c.drawRect(right, top, width.toFloat(), bottom, shade)
        c.drawRect(0f, bottom, width.toFloat(), height.toFloat(), shade)
        // 선택 테두리
        c.drawRect(left, top, right, bottom, stroke)
    }

    fun getCropRect(): RectF? {
        if (!hasRect) return null
        val left = min(sx, ex); val top = min(sy, ey); val right = max(sx, ex); val bottom = max(sy, ey)
        return if (right - left < 10f || bottom - top < 10f) null else RectF(left, top, right, bottom)
    }
}