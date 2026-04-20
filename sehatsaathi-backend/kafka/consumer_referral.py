from confluent_kafka import Consumer, KafkaError
from models.schemas import ReferralEvent
from db.mongo_client import get_sync_db
from db.collections import REFERRALS
import json
import os
from datetime import datetime

consumer = Consumer({
    "bootstrap.servers": os.getenv("KAFKA_BROKER", "localhost:9092"),
    "group.id": "referral-group",
    "auto.offset.reset": "earliest",
    "enable.auto.commit": True
})

consumer.subscribe(["referral-events"])

def process_referral(event: ReferralEvent):
    db = get_sync_db()

    db[REFERRALS].insert_one({
        "patientId":       event.patient_id,
        "chwId":           event.chw_id,
        "diagnosis":       event.diagnosis,
        "confidence":      event.confidence,
        "referralType":    event.referral_type,
        "nearestFacility": event.nearest_facility,
        "status":          "pending",
        "timestamp":       event.timestamp,
        "createdAt":       datetime.utcnow()
    })
    print(f"[referral-consumer] Saved | type={event.referral_type} | patient={event.patient_id}")

    if event.referral_type == "emergency":
        handle_emergency(event)
    elif event.referral_type == "clinic":
        handle_clinic(event)
    else:
        handle_home_care(event)

def handle_emergency(event: ReferralEvent):
    print(f"[referral-consumer] EMERGENCY → notify facility={event.nearest_facility} | patient={event.patient_id}")

def handle_clinic(event: ReferralEvent):
    print(f"[referral-consumer] CLINIC → notify {event.nearest_facility} | patient={event.patient_id}")

def handle_home_care(event: ReferralEvent):
    print(f"[referral-consumer] HOME CARE → logged | patient={event.patient_id}")

def run():
    print("[referral-consumer] Starting...")
    while True:
        msg = consumer.poll(timeout=1.0)
        if msg is None:
            continue
        if msg.error():
            if msg.error().code() != KafkaError._PARTITION_EOF:
                print(f"[referral-consumer] Error: {msg.error()}")
            continue
        try:
            data = json.loads(msg.value().decode("utf-8"))
            event = ReferralEvent(**data)
            process_referral(event)
        except Exception as e:
            print(f"[referral-consumer] Failed: {e}")

if __name__ == "__main__":
    run()