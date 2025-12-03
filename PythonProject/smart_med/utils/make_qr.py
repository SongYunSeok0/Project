import qrcode
from pathlib import Path

def create_qr(chip_id):
    qr_text = f"pillbox://register?chip_id={chip_id}"
    img = qrcode.make(qr_text)

    # static/qr 폴더에 저장
    output_dir = Path("static/qr")
    output_dir.mkdir(parents=True, exist_ok=True)

    filename = output_dir / f"{chip_id}.png"
    img.save(filename)

    print(f"[QR] Saved: {filename}")
    return str(filename)
