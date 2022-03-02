package org.apache.beam.samples.main;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import config.Options;
import org.apache.beam.samples.model.CustomData;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.*;
import org.apache.beam.samples.transform.ClassifyData;
import org.apache.beam.samples.transform.DeserializeData;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;

import java.util.Arrays;


public class KafkaToBigQuery {
    static final TupleTag<CustomData> oddTag = new TupleTag<>() {
    };
    static final TupleTag<CustomData> evenTag = new TupleTag<>() {
    };
    static final TupleTag<String> errorTag = new TupleTag<>() {
    };


    public static void main(String[] args) {
        Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
        options.setStreaming(true);

        Pipeline pipeline = Pipeline.create(options);
        PCollectionTuple mixedCollection = pipeline
                .apply("Consume from kafka", KafkaIO.<String, String>read()
                        .withBootstrapServers(options.getBootstrapServer())
                        .withTopic(options.getInputTopic())
                        .withKeyDeserializer(StringDeserializer.class)
                        .withValueDeserializer(StringDeserializer.class)
                        .withoutMetadata())
                .apply("Get message contents", Values.create())
                .apply("Fixed-size Window", Window.into(FixedWindows.of(Duration.standardMinutes(1))))
                .apply("Deserialize Json", ParDo.of(
                        new DeserializeData()
                ))
                .apply("Check odd or even", ParDo.of(
                        new ClassifyData(oddTag, errorTag)
                ).withOutputTags(evenTag, TupleTagList.of(oddTag).and(errorTag)));

        // 1. 第一個 Output
        mixedCollection.get(evenTag)
                .apply("Convert to BigQuery TableRow", MapElements.into(TypeDescriptor.of(TableRow.class))
                        .via(tmpObj -> new TableRow()
                                .set("processing_time", tmpObj.processingTime.toString())
                                .set("count", tmpObj.count)
                                .set("type", tmpObj.type)))
                .apply("Write to BigQuery", BigQueryIO.writeTableRows()
                        .to(options.getOutputEvenTable())
                        .withSchema(
                                new TableSchema().setFields(Arrays.asList(
                                        new TableFieldSchema().setName("processing_time").setType("TIMESTAMP"),
                                        new TableFieldSchema().setName("count").setType("INTEGER"),
                                        new TableFieldSchema().setName("type").setType("STRING"))))
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));


        // 2. 第二個 Output
        mixedCollection.get(oddTag)
                .apply("Convert to BigQuery TableRow", MapElements.into(TypeDescriptor.of(TableRow.class))
                        .via(tmpObj -> new TableRow()
                                .set("processing_time", tmpObj.processingTime.toString())
                                .set("count", tmpObj.count)
                                .set("type", tmpObj.type)))
                .apply("Write to BigQuery", BigQueryIO.writeTableRows()
                        .to(options.getOutputOddTable())
                        .withSchema(
                                new TableSchema().setFields(Arrays.asList(
                                        new TableFieldSchema().setName("processing_time").setType("TIMESTAMP"),
                                        new TableFieldSchema().setName("count").setType("INTEGER"),
                                        new TableFieldSchema().setName("type").setType("STRING"))))
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

        // 3. Error Handle
        mixedCollection.get(errorTag);


        pipeline.run();


    }
}
