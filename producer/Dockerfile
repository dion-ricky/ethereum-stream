FROM rust:1.85.0

WORKDIR /usr/src/producer
COPY . .

RUN cargo install --path .
CMD ["echo 1"]