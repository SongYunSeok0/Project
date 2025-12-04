# rag/health_food.py
import re
from typing import List, Dict
from collections import OrderedDict
from django.db.models import Q
from pgvector.django import CosineDistance

from .models import Chunk
from .embeddings import get_embedding
from .utils import normalize, short


def extract_specific_keywords(query: str) -> List[str]:
    """질문에서 구체적인 성분/제품 키워드만 추출"""
    keywords = []
    
    # ✅ 패턴 1: 비타민 + 영문자
    vitamin_pattern = r'비타민\s*([A-Za-z])\b'
    vitamin_matches = re.findall(vitamin_pattern, query)
    for letter in vitamin_matches:
        keywords.append(f"비타민{letter.upper()}")
    
    # ✅ 패턴 2: 한글 비타민
    q_norm = normalize(query)
    korean_vitamin_map = {
        '비타민씨': '비타민C',
        '비타민디': '비타민D',
        '비타민비': '비타민B',
        '비타민이': '비타민E',
        '비타민에이': '비타민A',
    }
    for korean, english in korean_vitamin_map.items():
        if korean in q_norm:
            keywords.append(english)
    
    # ✅ 패턴 3: 비타민 복합
    if '종합비타민' in query or '멀티비타민' in query:
        keywords.append('비타민')
    
    # ✅ 패턴 4: 주요 미네랄
    minerals = {
        '아연': ['아연'],
        '엽산': ['엽산'],
        '칼슘': ['칼슘'],
        '마그네슘': ['마그네슘'],
        '철분': ['철분', '철'],
        '셀레늄': ['셀레늄', '셀렌'],
        '크롬': ['크롬'],
        '요오드': ['요오드'],
        '망간': ['망간'],
        '구리': ['구리'],
        '비오틴': ['비오틴'],
    }
    
    for mineral, variants in minerals.items():
        for variant in variants:
            if variant in query:
                keywords.append(mineral)
                break
    
    # ✅ 패턴 5: 주요 성분
    major_components = {
        # 오메가
        '오메가3': ['오메가3', '오메가-3', '오메가 3', '알티지'],
        'DHA': ['dha', 'DHA'],
        'EPA': ['epa', 'EPA'],
        
        # 유산균
        '유산균': ['유산균', '프로바이오틱스', '락토바실러스', '비피더스'],
        
        # 눈 건강
        '루테인': ['루테인'],
        '지아잔틴': ['지아잔틴'],
        
        # 관절
        '글루코사민': ['글루코사민'],
        'MSM': ['msm', 'MSM', '엠에스엠'],
        '콘드로이틴': ['콘드로이틴'],
        '콜라겐': ['콜라겐'],
        
        # 항산화
        '코엔자임': ['코엔자임', 'Q10', 'q10', '큐텐'],
        
        # 간 건강
        '밀크씨슬': ['밀크씨슬', '실리마린'],
        
        # 에너지
        '홍삼': ['홍삼'],
        '인삼': ['인삼', '고려인삼'],
        
        # 기타
        '프로폴리스': ['프로폴리스'],
        '로얄젤리': ['로얄젤리'],
        '키토산': ['키토산'],
        '식이섬유': ['식이섬유'],
        '가르시니아': ['가르시니아'],
        '쏘팔메토': ['쏘팔메토'],
    }
    
    for component, variants in major_components.items():
        for variant in variants:
            if variant.lower() in query.lower():
                keywords.append(component)
                break
    
    # ✅ 패턴 6: 신체 부위 / 기능
    body_parts_and_functions = {
        # 신체 부위 - 정확한 매칭
        '관절': ['관절'],
        '뼈': ['뼈', '골다공증', '골밀도'],
        '눈': ['눈', '시력', '안구'],
        '간': ['간 ', '간기능', '간 건강', '간건강'],  # ✅ '간 ' 띄어쓰기 추가
        '위': ['위 ', '위장', '위 건강'],  # ✅ '위 ' 띄어쓰기 추가
        '장': ['장 건강', '장건강', '장 ', '장내', '프로바이오틱스', '유산균', '장'],  # ✅ 구체적으로
        '혈관': ['혈관', '혈행', '혈액순환'],
        '심장': ['심장', '심혈관'],
        '뇌': ['뇌', '기억력', '인지기능'],
        '피부': ['피부', '피부건강'],
        '모발': ['모발', '머리카락', '탈모'],
        '손톱': ['손톱'],

        # 신체 기능
        '면역': ['면역', '면역력', '면역기능'],
        '항산화': ['항산화', '항산화작용'],
        '에너지': ['에너지', '에너지생성'],
        '대사': ['대사', '신진대사'],
        '배변': ['배변', '배변활동'],
        '소화': ['소화'],
    }
    
    for keyword, variants in body_parts_and_functions.items():
        for variant in variants:
            if variant in query:
                keywords.append(keyword)
                break
    
    # ✅ 패턴 7: 증상 / 목적
    symptoms_and_purposes = {
        '피로': ['피로', '피로회복', '피곤'],
        '숙면': ['수면', '숙면', '불면'],
        '집중': ['집중', '집중력'],
        '스트레스': ['스트레스'],
        '다이어트': ['다이어트', '체중감량', '감량', '체지방'],  # ✅ 추가
        '성장': ['성장', '키성장'],
        '갱년기': ['갱년기'],
        '임신': ['임신', '임산부'],
        '노화': ['노화', '항노화'],
    }
    
    for symptom, variants in symptoms_and_purposes.items():
        for variant in variants:
            if variant in query:
                keywords.append(symptom)
                break
    
    # 중복 제거
    keywords = list(dict.fromkeys(keywords))
    
    return keywords


def search_health_food_chunks(query: str, k: int = 10, max_distance: float = 0.6) -> List[Chunk]:
    """건강기능식품 검색 - 임베딩 유사도 + 키워드 필터링"""
    q_emb = get_embedding(query)
    
    keywords = extract_specific_keywords(query)
    
    qs = Chunk.objects.filter(section__startswith="hf_function")
    
    if keywords:
        keyword_q = Q()
        for kw in keywords:
            keyword_q |= Q(text__icontains=kw) | Q(item_name__icontains=kw)
        
        filtered_qs = qs.filter(keyword_q)
        
        if filtered_qs.exists():
            qs = filtered_qs
            print(f"[HF-RAG] Filtered by keywords: {keywords}")
        else:
            print(f"[HF-RAG] No results for keywords: {keywords}, using all")
    else:
        print(f"[HF-RAG] No specific keywords found, using embedding similarity only")
    
    if not qs.exists():
        qs = Chunk.objects.filter(section__startswith="hf_")
    
    qs = qs.annotate(distance=CosineDistance("embedding", q_emb)).order_by("distance")[:k]
    
    chunks = list(qs)
    if not chunks:
        return []
    
    first_dist = float(chunks[0].distance) if chunks[0].distance is not None else None
    print(f"[HF-RAG] top_k={len(chunks)}, first_dist={first_dist}, keywords={keywords}")
    
    if first_dist is not None and first_dist > max_distance:
        print(f"[HF-RAG] distance too far ({first_dist} > {max_distance}) → 사용 안 함")
        return []
    
    return chunks


def collect_hf_products(chunks: List[Chunk]) -> "Dict[str, dict]":
    products: "OrderedDict[str, dict]" = OrderedDict()

    for c in chunks:
        name = (c.item_name or "제품명 미상").strip()
        info = products.setdefault(name, {"function": "", "usage": "", "caution": ""})

        raw = (c.text or "").strip()
        sec = (c.section or "").strip()

        if sec.startswith("hf_function") and not info["function"]:
            info["function"] = raw
        elif sec.startswith("hf_usage") and not info["usage"]:
            info["usage"] = raw
        elif sec.startswith("hf_caution") and not info["caution"]:
            info["caution"] = raw

    missing = [n for n, d in products.items() if not d["function"]]
    if missing:
        extra = Chunk.objects.filter(item_name__in=missing, section__startswith="hf_function")
        for c in extra:
            name = (c.item_name or "").strip()
            if products[name]["function"]:
                continue
            products[name]["function"] = (c.text or "").strip()

    return products


def clean_text_field(text: str) -> str:
    """텍스트에서 메타정보 라벨 제거 및 띄어쓰기 복원"""
    if not text:
        return text
    
    # 제품명, 제조사, 기능성 라벨 제거
    text = re.sub(r'제품명:\s*[^\n]+\n?', '', text)
    text = re.sub(r'제조사:\s*[^\n]+\n?', '', text)
    text = re.sub(r'^기능성:\s*', '', text, flags=re.MULTILINE)
    text = re.sub(r'^섭취 방법:\s*', '', text, flags=re.MULTILINE)
    text = re.sub(r'^주의사항:\s*', '', text, flags=re.MULTILINE)
    
    # ✅ 띄어쓰기 복원: 특정 패턴에서 띄어쓰기 추가
    # "증진피로개선" → "증진 피로개선"
    text = re.sub(r'(증진|개선|억제|도움|필요)(피로|면역|혈액|기억|항산화|에너지)', r'\1 \2', text)
    
    # "필요뼈의" → "필요 뼈의"
    text = re.sub(r'(필요|도움)(뼈|관절|눈|간|위|장|피부)', r'\1 \2', text)
    
    return text.strip()


def format_hf_answer(items: List[tuple[str, dict]]) -> str:
    if not items:
        return "해당 질문과 관련된 기능성 정보가 포함된 건강기능식품 데이터를 찾지 못했습니다."

    lines: List[str] = []
    lines.append("질문과 관련해 도움이 될 수 있는 건강기능식품/영양제 예시는 다음과 같습니다.\n")
    lines.append("※ 아래 내용은 일반 정보로, 실제 복용 전에는 반드시 의사 또는 약사와 상담해야 합니다.\n")

    for idx, (name, info) in enumerate(items, start=1):
        lines.append(f"{idx}. {name}")
        
        func_text = clean_text_field(info.get('function', ''))
        if func_text:
            lines.append("- 기능성")
            lines.append(f"  {short(func_text, 200)}")
        
        usage_text = clean_text_field(info.get('usage', ''))
        if usage_text:
            lines.append("- 섭취 방법")
            lines.append(f"  {short(usage_text, 150)}")  # ✅ 평균 73자이므로 150자면 충분
        
        caution_text = clean_text_field(info.get('caution', ''))
        if caution_text:
            lines.append("- 주의사항")
            lines.append(f"  {short(caution_text, 200)}")
        
        lines.append("")

    return "\n".join(lines).strip()


def build_health_food_answer(question: str, chunks: List[Chunk]) -> str:
    if not chunks:
        return "해당 질문과 관련된 기능성 정보가 포함된 건강기능식품 데이터를 찾지 못했습니다."

    products = collect_hf_products(chunks)

    candidates: List[tuple[str, dict]] = []
    for name, info in products.items():
        if info["function"]:
            candidates.append((name, info))

    if not candidates:
        return "해당 질문과 관련된 기능성 정보가 포함된 건강기능식품 데이터를 찾지 못했습니다."

    top_items = candidates[:5]

    return format_hf_answer(top_items)