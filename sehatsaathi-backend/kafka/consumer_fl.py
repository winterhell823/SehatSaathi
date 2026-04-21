from confluent_kafka import Consumer, KafkaError
from models.schemas import FLGradientEvent
from db.mongo_client import get_sync_db
from db.collections import MODEL_VERSIONS
from fl.model_registry import register_new_round
from fl.fl_server import run_aggregation
import json
import os
from datetime import datetime

consumer = Consumer({
    "bootstrap.servers": os.getenv("KAFKA_BROKER", "localhost:9092"),
    "group.id": "fl-group",
    "auto.offset.reset": "earliest",
    "enable.auto.commit": True
})

consumer.subscribe(["fl-gradients"])

pending_gradients = []
MIN_CLIENTS = int(os.getenv("FL_MIN_CLIENTS", "5"))

def process_gradient(event: FLGradientEvent):
    global pending_gradients
    pending_gradients.append(event)
    print(f"[fl-consumer] Gradient received | device={event.device_id} | samples={event.num_samples} | pending={len(pending_gradients)}")

    if len(pending_gradients) >= MIN_CLIENTS:
        trigger_aggregation(pending_gradients)
        pending_gradients = []

def trigger_aggregation(gradients: list):
    new_version = register_new_round(
        client_count=len(gradients),
        total_samples=sum(g.num_samples for g in gradients)
    )
    gradient_dicts = [
        {
            "device_id":        g.device_id,
            "num_samples":      g.num_samples,
            "gradient_payload": {"weight": 1.0}
        }
        for g in gradients
    ]
    run_aggregation(gradient_dicts, new_version)

def run():
    print("[fl-consumer] Starting...")
    while True:
        msg = consumer.poll(timeout=1.0)
        if msg is None:
            continue
        if msg.error():
            if msg.error().code() != KafkaError._PARTITION_EOF:
                print(f"[fl-consumer] Error: {msg.error()}")
            continue
        try:
            data = json.loads(msg.value().decode("utf-8"))
            event = FLGradientEvent(**data)
            process_gradient(event)
        except Exception as e:
            print(f"[fl-consumer] Failed: {e}")

if __name__ == "__main__":
    run()