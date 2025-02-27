package com.dionricky;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.Tumble;

import static org.apache.flink.table.api.Expressions.*;


public class AggTransactions {
    public static void main(String[] args) {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().build();
        TableEnvironment tEnv = TableEnvironment.create(settings);

        tEnv.executeSql("CREATE OR REPLACE TABLE transactions (\n" +
                "    `hash`          STRING," +
                "    from_address    STRING,\n" +
                "    to_address      STRING,\n" +
                "    `value`         DOUBLE,\n" +
                "    block_timestamp TIMESTAMP(3),\n" +
                "    WATERMARK FOR block_timestamp AS block_timestamp - INTERVAL '5' SECOND\n" +
                ") WITH (\n" +
                "    'connector' = 'kafka',\n" +
                "    'topic'     = 'eth-transactions',\n" +
                "    'properties.bootstrap.servers' = 'kafka:9092',\n" +
                "    'scan.startup.mode' = 'earliest-offset',\n" +
                "    'format'    = 'json'\n" +
                ")");

        tEnv.executeSql("CREATE TABLE txn_report (\n" +
                "    log_ts        TIMESTAMP(3),\n" +
                "    total_value   DOUBLE\n," +
                "    PRIMARY KEY (log_ts) NOT ENFORCED" +
                ") WITH (\n" +
                "  'connector'  = 'jdbc',\n" +
                "  'url'        = 'jdbc:postgresql://postgres:5432/ethereum',\n" +
                "  'table-name' = 'txn_report',\n" +
                "  'username'   = 'ethereum',\n" +
                "  'password'   = 'ethereum'\n" +
                ")");

        Table transactions = tEnv.from("transactions");
        transactions.window(Tumble.over(lit(1).hour()).on($("block_timestamp")).as("log_ts"))
                .groupBy($("log_ts"))
                .select(
                    $("log_ts").start().as("log_ts"),
                    $("value").sum().as("total_value")
                ).executeInsert("txn_report");
    }
}