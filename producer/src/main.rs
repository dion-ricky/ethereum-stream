use kafka::producer::{Producer, Record};
use polars::prelude::*;
use std::{env, io::Write};

struct KafkaWriter {
    buffer: Vec<u8>,
    topic: String,
    producer: Producer,
}

impl KafkaWriter {
    fn new(topic: String) -> Self {
        let producer = Producer::from_hosts(vec!["kafka:9092".to_string()])
            .with_ack_timeout(std::time::Duration::from_secs(1))
            .with_required_acks(kafka::client::RequiredAcks::One)
            .create()
            .expect("Failed to create producer");

        KafkaWriter {
            buffer: Vec::new(),
            topic: topic,
            producer: producer,
        }
    }
}

impl Write for KafkaWriter {
    fn write(&mut self, buf: &[u8]) -> std::io::Result<usize> {
        self.buffer.extend_from_slice(buf);

        let s = String::from_utf8_lossy(buf);

        for record in s.split("\n") {
            self.producer
                .send(&Record::from_key_value(self.topic.as_str(), "", record))
                .expect("Failed to send message");
        }

        Ok(buf.len())
    }

    fn flush(&mut self) -> std::io::Result<()> {
        Ok(())
    }
}

fn main() {
    let args: Vec<String> = env::args().collect();
    let path = &args[1];
    let topic = &args[2];

    let mut file = std::fs::File::open(path).unwrap();

    let mut df = ParquetReader::new(&mut file).finish().unwrap();

    let mut mf = KafkaWriter::new(topic.to_string());

    JsonWriter::new(&mut mf)
        .with_json_format(JsonFormat::JsonLines)
        .finish(&mut df)
        .unwrap();
}
