from confluent_kafka import Producer
from models.schemas import (
    DiagnosisEvent, FLGradientEvent,
    SymptomStreamEvent, ReferralEvent
)
import os

_producer: Producer = None

TOPIC_DIAGNOSIS = "diagnosis-events"
TOPIC_FL        = "fl-gradients"
TOPIC_SYMPTOMS  = "symptom-stream"
TOPIC_REFERRAL  = "referral-events"

def init_producer():
    global _producer
    _producer = Producer({
        "bootstrap.servers": os.getenv("KAFKA_BROKER", "localhost:9092"),
        "client.id": "vitalai-backend",
        "acks": "all",
        "retries": 3
    })
    print("Kafka producer initialized")

def close_producer():
    global _producer
    if _producer:
        _producer.flush()
        print("Kafka producer flushed and closed")

def _delivery_report(err, msg):
    if err:
        print(f"[Kafka] Delivery failed | topic={msg.topic()} | err={err}")
    else:
        print(f"[Kafka] Delivered | topic={msg.topic()} | partition={msg.partition()} | offset={msg.offset()}")

def publish_diagnosis(event: DiagnosisEvent):
    _producer.produce(
        topic=TOPIC_DIAGNOSIS,
        key=event.patient_id,
        value=event.model_dump_json(),
        callback=_delivery_report
    )
    _producer.poll(0)

def publish_fl_gradient(event: FLGradientEvent):
    _producer.produce(
        topic=TOPIC_FL,
        key=event.device_id,
        value=event.model_dump_json(),
        callback=_delivery_report
    )
    _producer.poll(0)

def publish_symptom(event: SymptomStreamEvent):
    _producer.produce(
        topic=TOPIC_SYMPTOMS,
        key=f"{event.district}_{event.state}",
        value=event.model_dump_json(),
        callback=_delivery_report
    )
    _producer.poll(0)

def publish_referral(event: ReferralEvent):
    _producer.produce(
        topic=TOPIC_REFERRAL,
        key=event.patient_id,
        value=event.model_dump_json(),
        callback=_delivery_report
    )
    _producer.poll(0)