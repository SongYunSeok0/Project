#!/usr/bin/env python
"""Django's command-line utility for administrative tasks."""
import os
import sys
from django.db import connections  # 수정된 부분


def main():
    """Run administrative tasks."""
    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "smart_med.settings")

    # 데이터베이스 연결이 초기화된 후 autocommit을 True로 설정
    connection = connections['default']  # 데이터베이스 연결 가져오기
    connection.connect()  # 연결이 되어 있지 않으면 연결을 시도
    connection.autocommit = True  # autocommit 활성화

    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Couldn't import Django. Are you sure it's installed and "
            "available on your PYTHONPATH environment variable? Did you "
            "forget to activate a virtual environment?"
        ) from exc
    execute_from_command_line(sys.argv)


if __name__ == "__main__":
    main()
