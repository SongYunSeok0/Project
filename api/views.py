from rest_framework.decorators import api_view
from rest_framework.response import Response
from datetime import datetime
import pymongo

client = pymongo.MongoClient("mongodb://localhost:27017/")
db = client["iot_db"]
collection = db["sensor_data"]

@api_view(['POST'])
def sensor_data(request):
    data = request.data
    data["timestamp"] = datetime.now()
    collection.insert_one(data)
    return Response({"status": "ok", "received": data})

@api_view(['GET'])
def latest_data(request):
    doc = collection.find().sort("timestamp", -1).limit(1)
    latest = list(doc)
    return Response(latest)
