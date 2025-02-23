FROM rust:1.85.0

WORKDIR /usr/src/producer
COPY . .

RUN cargo install --path .
CMD ["producer", "/usr/src/producer/data/ethereum_transactions-000000000000", "eth-transactions"]