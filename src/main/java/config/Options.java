package config;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation;

public interface Options extends StreamingOptions {
    @Description("Apache Kafka topic to read from.")
    @Validation.Required
    String getInputTopic();

    void setInputTopic(String value);

    @Description("BigQuery table to write to, in the form "
            + "'project:dataset.table' or 'dataset.table'.")
    @Default.String("beam_samples.streaming_beam_sql")
    String getOutputEvenTable();

    void setOutputEvenTable(String value);

    @Description("BigQuery table to write to, in the form "
            + "'project:dataset.table' or 'dataset.table'.")
    @Default.String("beam_samples.streaming_beam_sql")
    String getOutputOddTable();

    void setOutputOddTable(String value);

    @Description("Apache Kafka bootstrap servers in the form 'hostname:port'.")
    @Default.String("localhost:9092")
    String getBootstrapServer();

    void setBootstrapServer(String value);
}
