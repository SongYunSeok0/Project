import requests

def push_is_time(device, value: bool):
    url = f"http://176.16.0.168/set_time"

    try:
        res = requests.post(url, json={"is_time": value}, timeout=3)
        res.raise_for_status()
        return True

    except Exception as e:
        print(f"[ERROR] push_is_time failed: {e}")
        return False