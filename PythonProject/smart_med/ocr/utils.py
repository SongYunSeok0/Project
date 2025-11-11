import pytesseract
from PIL import Image

def extract_text_from_image(image_path):
    """
    이미지 경로를 받아 OCR로 텍스트 추출
    """
    image = Image.open(image_path)
    text = pytesseract.image_to_string(image, lang='kor+eng')
    return text.strip()
