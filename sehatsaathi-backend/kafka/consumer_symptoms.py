from confluent_kafka import Consumer, KafkaError
from models.schemas import SymptomStreamEvent
from db.mongo_client import get_sync_db
from db.collections import OUTBREAK_ALERTS
import json
import os
from datetime import datetime, timedelta
from collections import defaultdict

consumer = Consumer({
    "bootstrap.servers": os.getenv("KAFKA_BROKER", "localhost:9092"),
    "group.id": "symptoms-group",
    "auto.offset.reset": "earliest",
    "enable.auto.commit": True
})

consumer.subscribe(["symptom-stream"])

OUTBREAK_THRESHOLD = int(os.getenv("OUTBREAK_THRESHOLD", "10"))
WINDOW_HOURS       = int(os.getenv("OUTBREAK_WINDOW_HOURS", "24"))

window: defaultdict = defaultdict(list)

def check_outbreak(event: SymptomStreamEvent):
    now    = datetime.utcnow()
    cutoff = now - timedelta(hours=WINDOW_HOURS)

    for symptom in event.symptoms:
        key = f"{event.district}_{event.state}_{symptom}"
        window[key] = [t for t in window[key] if t > cutoff]
        window[key].append(now)
        count = len(window[key])

        print(f"[symptoms-consumer] district={event.district} | symptom={symptom} | count={count}")

        if count == OUTBREAK_THRESHOLD:
            fire_alert(event.district, event.state, symptom, count)

def fire_alert(district: str, state: str, symptom: str, count: int):
    db = get_sync_db()
    db[OUTBREAK_ALERTS].insert_one({
        "district":    district,
        "state":       state,
        "symptom":     symptom,
        "caseCount":   count,
        "windowHours": WINDOW_HOURS,
        "status":      "active",
        "createdAt":   datetime.utcnow()
    })
    print(f"[symptoms-consumer] OUTBREAK ALERT | {count} cases of '{symptom}' in {district}, {state}")

def run():
    print("[symptoms-consumer] Starting...")
    while True:
        msg = consumer.poll(timeout=1.0)
        if msg is None:
            continue
        if msg.error():
            if msg.error().code() != KafkaError._PARTITION_EOF:
                print(f"[symptoms-consumer] Error: {msg.error()}")
            continue
        try:
            data = json.loads(msg.value().decode("utf-8"))
            event = SymptomStreamEvent(**data)
            check_outbreak(event)
        except Exception as e:
            print(f"[symptoms-consumer] Failed: {e}")

if __name__ == "__main__":
    run()