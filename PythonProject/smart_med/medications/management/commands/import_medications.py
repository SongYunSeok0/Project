import csv
from django.core.management.base import BaseCommand
from medications.models import MedicationInfo
from datetime import datetime

class Command(BaseCommand):
    help = "Import medication info from CSV file"

    def add_arguments(self, parser):
        parser.add_argument('csv_file', type=str, help='Path to the CSV file')

    def handle(self, *args, **options):
        csv_file = options['csv_file']
        count = 0
        with open(csv_file, encoding='utf-8') as file:
            reader = csv.DictReader(file)
            for row in reader:
                MedicationInfo.objects.update_or_create(
                    item_seq=row.get('ITEM_SEQ'),
                    defaults={
                        'name': row.get('ITEM_NAME'),
                        'manufacturer': row.get('ENTP_NAME'),
                        'efficacy': row.get('EFCY_QESITM'),
                        'usage': row.get('USE_METHOD_QESITM'),
                        'caution': row.get('ATPN_WARN_QESITM'),
                        'contraindication': row.get('ATPN_QESITM'),
                        'interaction': row.get('INTRC_QESITM'),
                        'side_effect': row.get('SE_QESITM'),
                        'storage': row.get('DEPOSIT_METHOD_QESITM'),
                        'image_url': row.get('ITEM_IMAGE'),
                        'open_date': self.parse_date(row.get('OPEN_DE')),
                        'update_date': self.parse_date(row.get('UPDATE_DE')),
                    }
                )
                count += 1
        self.stdout.write(self.style.SUCCESS(f'{count} records imported successfully.'))

    def parse_date(self, date_str):
        try:
            return datetime.strptime(date_str, "%Y%m%d").date()
        except Exception:
            return None
