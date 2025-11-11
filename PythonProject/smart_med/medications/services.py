from .models import Medication, MedicationHistory

def process_sensor(user_id, medication_id, weight):
    med = Medication.objects.get(id=medication_id, user_id=user_id)

    if weight < 0.3:
        status = "missed"
        message = "약이 복용되지 않았습니다."
    else:
        status = "taken"
        message = "약이 정상적으로 복용되었습니다."

    # 복용 이력 저장
    MedicationHistory.objects.create(medication=med, status=status)

    return {"status": status, "message": message}
