package com.dionricky;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;

public class RawTransactions {

    public static void main(String[] args) {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().build();
        TableEnvironment tEnv = TableEnvironment.create(settings);

        tEnv.executeSql("CREATE OR REPLACE TABLE transactions (\n" +
                "    `hash`          STRING," +
                "    from_address    STRING,\n" +
                "    to_address      STRING,\n" +
                "    `value`         DOUBLE,\n" +
                "    block_timestamp TIMESTAMP(3)\n" +
                ") WITH (\n" +
                "    'connector' = 'kafka',\n" +
                "    'topic'     = 'eth-transactions',\n" +
                "    'properties.bootstrap.servers' = 'kafka:9092',\n" +
                "    'scan.startup.mode' = 'earliest-offset',\n" +
                "    'format'    = 'json'\n" +
                ")");

        tEnv.executeSql("CREATE TABLE raw_txn (\n" +
                "    `hash`          STRING," +
                "    from_address    STRING,\n" +
                "    to_address      STRING,\n" +
                "    `value`         DOUBLE,\n" +
                "    block_timestamp TIMESTAMP(3),\n" +
                "    PRIMARY KEY (`hash`) NOT ENFORCED" +
                ") WITH (\n" +
                "  'connector'  = 'jdbc',\n" +
                "  'url'        = 'jdbc:postgresql://postgres:5432/ethereum',\n" +
                "  'table-name' = 'raw_txn',\n" +
                "  'username'   = 'ethereum',\n" +
                "  'password'   = 'ethereum'\n" +
                ")");

        Table transactions = tEnv.from("transactions");
        transactions.executeInsert("raw_txn");

    }
}
