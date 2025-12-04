# utils/qr.py
import qrcode
from pathlib import Path

def create_qr(device_uuid, device_token, chip_id):
    qr_text = (
        f"pillbox://register?"
        f"uuid={device_uuid}&"
        f"token={device_token}&"
        f"chip_id={chip_id}"
    )

    output_dir = Path("static/qr")
    output_dir.mkdir(parents=True, exist_ok=True)

    filename = output_dir / f"{device_uuid}.png"

    img = qrcode.make(qr_text)
    img.save(filename)

    return str(filename)
