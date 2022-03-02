package org.apache.beam.samples.model;

import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.joda.time.Instant;


@DefaultCoder(AvroCoder.class)
public class CustomData {
    @Nullable
    public Long count;
    @Nullable
    public String type;
    @Nullable
    public Instant processingTime;
}
