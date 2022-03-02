package org.apache.beam.samples.transform;

import com.google.gson.Gson;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.samples.model.CustomData;

public class DeserializeData extends DoFn<String, CustomData> {
    @ProcessElement
    public void processElement(ProcessContext context) throws Exception {
        Gson GSON = new Gson();
        CustomData tmpObj = GSON.fromJson(context.element(), CustomData.class);
        System.out.println(tmpObj.processingTime);
        context.output(tmpObj);
    }

}
