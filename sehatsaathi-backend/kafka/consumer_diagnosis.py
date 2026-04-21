from confluent_kafka import Consumer, KafkaError
from models.schemas import DiagnosisEvent
from db.mongo_client import get_sync_db
from db.collections import ENCOUNTERS, PATIENTS
import json
import os
from datetime import datetime

consumer = Consumer({
    "bootstrap.servers": os.getenv("KAFKA_BROKER", "localhost:9092"),
    "group.id": "diagnosis-group",
    "auto.offset.reset": "earliest",
    "enable.auto.commit": True
})

consumer.subscribe(["diagnosis-events"])

def save_encounter(event: DiagnosisEvent):
    db = get_sync_db()

    encounter_doc = {
        "patientId":    event.patient_id,
        "chwId":        event.chw_id,
        "village":      event.village,
        "district":     event.district,
        "state":        event.state,
        "lat":          event.lat,
        "lng":          event.lng,
        "symptoms":     event.symptoms,
        "imageScore":   event.image_score,
        "topDiagnosis": event.top_diagnosis,
        "allDiagnoses": event.all_diagnoses,
        "confidence":   event.confidence,
        "trustLevel":   event.trust_level,
        "timestamp":    event.timestamp,
        "createdAt":    datetime.utcnow()
    }

    db[ENCOUNTERS].insert_one(encounter_doc)
    print(f"[diagnosis-consumer] Saved encounter | patient={event.patient_id} | diagnosis={event.top_diagnosis}")

    db[PATIENTS].update_one(
        {"patientId": event.patient_id},
        {"$set": {
            "lastSeenAt": event.timestamp,
            "village":    event.village,
            "district":   event.district
        }},
        upsert=True
    )

def run():
    print("[diagnosis-consumer] Starting...")
    while True:
        msg = consumer.poll(timeout=1.0)
        if msg is None:
            continue
        if msg.error():
            if msg.error().code() != KafkaError._PARTITION_EOF:
                print(f"[diagnosis-consumer] Error: {msg.error()}")
            continue
        try:
            data = json.loads(msg.value().decode("utf-8"))
            event = DiagnosisEvent(**data)
            save_encounter(event)
        except Exception as e:
            print(f"[diagnosis-consumer] Failed: {e}")

if __name__ == "__main__":
    run()